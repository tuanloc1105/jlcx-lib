package vn.com.lcx.vertx.base.validate;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.ObjectUtils;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.LessThan;
import vn.com.lcx.vertx.base.annotation.NotNull;
import vn.com.lcx.vertx.base.annotation.Values;
import vn.com.lcx.vertx.base.enums.ErrorCodeEnums;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AutoValidation {

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
                LogUtils.writeLog(e.getMessage(), e);
            }
        }
        return errorFields;
    }

}
