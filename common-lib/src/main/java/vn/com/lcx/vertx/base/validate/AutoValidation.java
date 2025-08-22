package vn.com.lcx.vertx.base.validate;

import com.google.gson.annotations.SerializedName;
import vn.com.lcx.common.exception.ValidationException;
import vn.com.lcx.common.utils.ObjectUtils;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.LessThan;
import vn.com.lcx.vertx.base.annotation.NotNull;
import vn.com.lcx.vertx.base.annotation.Values;
import vn.com.lcx.vertx.base.enums.ErrorCodeEnums;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AutoValidation {

    private AutoValidation() {
    }

    public static List<String> validate(Object validateObject) {
        if (!(validateObject.getClass().getName().contains("vn.")) && !(validateObject.getClass().getName().contains("com."))) {
            return new ArrayList<>();
        }
        final var errorFields = new ArrayList<String>();
        var fields = new ArrayList<>(Arrays.asList(validateObject.getClass().getDeclaredFields()));
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(validateObject);

                String fieldName = field.getName();
                SerializedName annotation = field.getAnnotation(SerializedName.class);
                if (Optional.ofNullable(annotation).isPresent()) {
                    fieldName = annotation.value();
                }

                NotNull notNull = field.getAnnotation(NotNull.class);
                GreaterThan greaterThan = field.getAnnotation(GreaterThan.class);
                LessThan lessThan = field.getAnnotation(LessThan.class);
                Values valuesPattern = field.getAnnotation(Values.class);

                if (Optional.ofNullable(notNull).isPresent()) {
                    if (ObjectUtils.isNullOrEmpty(fieldValue)) {
                        errorFields.add(fieldName);
                    }
                }
                if (
                        !(field.getType().getName().contains("java.")) &&
                                !(field.getType().getName().contains("org.")) &&
                                !(validateObject.getClass().isAssignableFrom(field.getType()))
                ) {
                    if (Optional.ofNullable(fieldValue).isPresent()) {
                        errorFields.addAll(validate(fieldValue));
                    }
                    continue;
                }

                if (field.getType().isAssignableFrom(String.class) && Optional.ofNullable(valuesPattern).isPresent()) {
                    if (!Optional.ofNullable(fieldValue).isPresent()) {
                        throw new InternalServiceException(
                                ErrorCodeEnums.INVALID_REQUEST,
                                String.format(
                                        "%s's value must not null",
                                        fieldName
                                )
                        );
                    }
                    List<String> patterns = Arrays.asList(valuesPattern.value());
                    if (!patterns.contains(fieldValue.toString())) {
                        throw new InternalServiceException(
                                ErrorCodeEnums.INVALID_REQUEST,
                                String.format(
                                        "%s's value must be like one of these: %s",
                                        fieldName,
                                        patterns.stream().collect(
                                                Collectors.joining(", ", "[", "]")
                                        )
                                )
                        );
                    }
                }

                if (
                        Optional.ofNullable(fieldValue).isPresent()
                                && (
                                Optional.ofNullable(lessThan).isPresent() || Optional.ofNullable(greaterThan).isPresent()
                        )
                ) {
                    double conditionNumber;
                    double fieldNumber;
                    if (field.getType().isAssignableFrom(BigDecimal.class)) {
                        fieldNumber = ((BigDecimal) fieldValue).doubleValue();
                    } else {
                        fieldNumber = Double.parseDouble(String.valueOf(fieldValue));
                    }
                    if (Optional.ofNullable(lessThan).isPresent()) {
                        conditionNumber = lessThan.value();
                        if (fieldNumber >= conditionNumber) {
                            throw new InternalServiceException(
                                    ErrorCodeEnums.INVALID_REQUEST,
                                    String.format(
                                            "%s's value must be less than [%.2f]",
                                            fieldName,
                                            conditionNumber
                                    )
                            );
                        }
                    }
                    if (Optional.ofNullable(greaterThan).isPresent()) {
                        conditionNumber = greaterThan.value();
                        if (fieldNumber <= conditionNumber) {
                            throw new InternalServiceException(
                                    ErrorCodeEnums.INVALID_REQUEST,
                                    String.format(
                                            "%s's value must be greater than [%.2f]",
                                            fieldName,
                                            conditionNumber
                                    )
                            );
                        }
                    }
                }

            } catch (InternalServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new ValidationException(e);
            }
        }
        return errorFields;
    }

    public static List<String> validateV2(Object validateObject) {
        if (validateObject == null) {
            return new ArrayList<>();
        }

        // only validate classes in your packages
        if (!(validateObject.getClass().getName().contains("vn."))
                && !(validateObject.getClass().getName().contains("com."))) {
            return new ArrayList<>();
        }

        final var errorFields = new ArrayList<String>();
        var fields = new ArrayList<>(Arrays.asList(validateObject.getClass().getDeclaredFields()));

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(validateObject);

                String fieldName = field.getName();
                SerializedName annotation = field.getAnnotation(SerializedName.class);
                if (annotation != null) {
                    fieldName = annotation.value();
                }

                NotNull notNull = field.getAnnotation(NotNull.class);
                GreaterThan greaterThan = field.getAnnotation(GreaterThan.class);
                LessThan lessThan = field.getAnnotation(LessThan.class);
                Values valuesPattern = field.getAnnotation(Values.class);

                // --- handle @NotNull ---
                if (notNull != null) {
                    if (ObjectUtils.isNullOrEmpty(fieldValue)) {
                        errorFields.add(fieldName);
                    }
                }

                // --- recursive validate for nested objects ---
                if (fieldValue != null) {
                    Class<?> fieldType = field.getType();

                    // 1. Handle Collection<T>
                    if (Collection.class.isAssignableFrom(fieldType)) {
                        Collection<?> collection = (Collection<?>) fieldValue;
                        for (Object item : collection) {
                            if (item != null) {
                                errorFields.addAll(validateV2(item));
                            }
                        }
                        continue;
                    }

                    // 2. Handle Map<K, V>
                    if (Map.class.isAssignableFrom(fieldType)) {
                        Map<?, ?> map = (Map<?, ?>) fieldValue;
                        for (Object entryValue : map.values()) {
                            if (entryValue != null) {
                                errorFields.addAll(validateV2(entryValue));
                            }
                        }
                        continue;
                    }

                    // 3. Handle parameterized type (e.g., Optional<T>)
                    if (field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) field.getGenericType();
                        for (Type actualType : pType.getActualTypeArguments()) {
                            if (actualType instanceof Class<?>) {
                                // recursive validate on generic type value
                                if (fieldValue instanceof Optional) {
                                    ((Optional<?>) fieldValue).ifPresent(val -> errorFields.addAll(validateV2(val)));
                                }
                            }
                        }
                    }

                    // 4. Handle custom objects
                    if (!(fieldType.getName().startsWith("java."))
                            && !(fieldType.getName().startsWith("org."))) {
                        errorFields.addAll(validateV2(fieldValue));
                        continue;
                    }
                }

                // --- @Values validation ---
                if (field.getType().isAssignableFrom(String.class) && valuesPattern != null) {
                    if (fieldValue == null) {
                        throw new InternalServiceException(
                                ErrorCodeEnums.INVALID_REQUEST,
                                String.format("%s's value must not null", fieldName)
                        );
                    }
                    List<String> patterns = Arrays.asList(valuesPattern.value());
                    if (!patterns.contains(fieldValue.toString())) {
                        throw new InternalServiceException(
                                ErrorCodeEnums.INVALID_REQUEST,
                                String.format(
                                        "%s's value must be like one of these: %s",
                                        fieldName,
                                        patterns.stream().collect(Collectors.joining(", ", "[", "]"))
                                )
                        );
                    }
                }

                // --- @GreaterThan / @LessThan validation ---
                if (fieldValue != null && (lessThan != null || greaterThan != null)) {
                    double fieldNumber;
                    if (field.getType().isAssignableFrom(BigDecimal.class)) {
                        //noinspection DataFlowIssue
                        fieldNumber = ((BigDecimal) fieldValue).doubleValue();
                    } else {
                        fieldNumber = Double.parseDouble(String.valueOf(fieldValue));
                    }

                    if (lessThan != null) {
                        double conditionNumber = lessThan.value();
                        if (fieldNumber >= conditionNumber) {
                            throw new InternalServiceException(
                                    ErrorCodeEnums.INVALID_REQUEST,
                                    String.format("%s's value must be less than [%.2f]", fieldName, conditionNumber)
                            );
                        }
                    }
                    if (greaterThan != null) {
                        double conditionNumber = greaterThan.value();
                        if (fieldNumber <= conditionNumber) {
                            throw new InternalServiceException(
                                    ErrorCodeEnums.INVALID_REQUEST,
                                    String.format("%s's value must be greater than [%.2f]", fieldName, conditionNumber)
                            );
                        }
                    }
                }

            } catch (InternalServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new ValidationException(e);
            }
        }

        return errorFields;
    }

}
