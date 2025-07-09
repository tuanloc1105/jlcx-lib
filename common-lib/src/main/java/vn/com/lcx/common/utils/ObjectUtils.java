package vn.com.lcx.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for object-related operations such as mapping fields, checking null/empty, and type reflection.
 * <p>
 * This class provides static methods to:
 * <ul>
 *   <li>Map fields from one object to another with matching field names</li>
 *   <li>Check if an object is null or empty (for collections and strings)</li>
 *   <li>Wrap primitive types to their object equivalents</li>
 *   <li>Get default values for primitive types</li>
 *   <li>Retrieve superclasses and interfaces</li>
 *   <li>Extract type parameters from interfaces</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated.
 *
 * @author LCX
 * @since 1.0
 */
public final class ObjectUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ObjectUtils() {
    }

    /**
     * Maps fields with matching names from a source object to a new instance of the target class.
     * Only non-static, non-final fields are mapped. Fields in superclasses are also considered.
     *
     * @param source   the source object
     * @param target   the target class
     * @param <SOURCE> the type of the source object
     * @param <TARGET> the type of the target object
     * @return a new instance of the target class with mapped fields, or null if instantiation fails
     */
    public static <SOURCE, TARGET> TARGET mapObjects(SOURCE source, Class<TARGET> target) {
        // Get all field of source class and target class
        List<Field> sourceClassFields = new ArrayList<>(Arrays.asList(source.getClass().getDeclaredFields()));
        List<Field> targetClassFields = new ArrayList<>(Arrays.asList(target.getDeclaredFields()));
        // Get all field of super class of both
        if (source.getClass().getSuperclass() != null) {
            List<Field> superClassField = Arrays.asList(source.getClass().getSuperclass().getDeclaredFields());
            sourceClassFields.addAll(superClassField);
        }
        if (target.getSuperclass() != null) {
            List<Field> superClassField = Arrays.asList(target.getSuperclass().getDeclaredFields());
            targetClassFields.addAll(superClassField);
        }
        // Filter: only get fields in source that its name is included in target
        List<String> targetClassFieldNames = targetClassFields.stream().map(Field::getName).collect(Collectors.toList());
        sourceClassFields = sourceClassFields
                .stream()
                .filter(field -> targetClassFieldNames.contains(field.getName()))
                .collect(Collectors.toList());
        targetClassFields.sort(Comparator.comparing(Field::getName));
        sourceClassFields.sort(Comparator.comparing(Field::getName));
        TARGET result;
        try {
            result = target.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
        // Start mapping
        // Lưu ý: khi dùng field.set(ObjectToSet, valueToSetToFeild) thì ObjectToSet phải là Object mà field đó thuộc về, nó hoạt động giống như setter,
        // tương tự với value = field.get(ObjectToGet), nó hoạt động giống như getter (phải dùng hàm field.setAccessible(true) trước)
        sourceClassFields.forEach(field -> {
            field.setAccessible(true);
            Object value;
            try {
                if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    value = field.get(source);
                    if (value != null) {
                        Field fieldToSet = targetClassFields.stream().filter(field1 -> field1.getName().equals(field.getName())).collect(Collectors.toList()).get(0);
                        fieldToSet.setAccessible(true);
                        fieldToSet.set(result, value);
                    }
                }
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        });
        return result;
    }

    /**
     * Checks if an object is null or empty. Supports collections and strings.
     *
     * @param object the object to check
     * @return true if the object is null, an empty collection, or a blank string; false otherwise
     */
    public static boolean isNullOrEmpty(Object object) {
        if (Optional.ofNullable(object).isEmpty()) {
            return true;
        }
        if (object instanceof Iterable) {
            Collection<?> objectChecking = (Collection<?>) object;
            return objectChecking.isEmpty();
        }
        if (object instanceof String) {
            String objectChecking = (String) object;
            return objectChecking.trim().isEmpty();
        }
        return false;
    }

    /**
     * Wrap primitive type to the corresponding object type.
     *
     * <p>For example, int.class to Integer.class, long.class to Long.class, etc.
     *
     * @param type the primitive type
     * @return the corresponding object type
     */
    public static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == void.class) {
            return Void.class;
        }
        return type;
    }

    /**
     * Return the default value of the given primitive type.
     *
     * @param type the primitive type
     * @return the default value of the given type.
     */
    public static Object getDefaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0f;
        }
        if (type == double.class) {
            return 0.0d;
        }
        if (type == char.class) {
            return '\u0000';
        }
        return null; // for objects
    }

    /**
     * Returns a list containing the superclass and interfaces of the given class, if not Object.
     *
     * @param target the class to inspect
     * @return a list of superclasses and interfaces, or empty if none
     */
    public static List<Class<?>> getExtendAndInterfaceClasses(Class<?> target) {
        List<Class<?>> result = new ArrayList<>();
        Class<?> extendingClass = target.getSuperclass();
        if (!Object.class.getName().equals(extendingClass.getName())) {
            result.add(extendingClass);
            result.addAll(
                    Arrays.stream(extendingClass.getInterfaces())
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        return result;
    }

    /**
     * Returns the type parameters of an interface, or an empty list if not found.
     *
     * @param clazz the interface class
     * @return a list of type parameters, or empty if not found
     * @throws IllegalArgumentException if type parameters cannot be determined
     */
    public static List<Type> getTypeParameters(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return Collections.emptyList();
        }
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            return Arrays.stream(
                            ((ParameterizedType) genericSuperclass).getActualTypeArguments()
                    )
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                return Arrays.stream(
                                ((ParameterizedType) genericInterface).getActualTypeArguments()
                        )
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return getTypeParameters(superclass);
        }

        throw new IllegalArgumentException("Cannot find type parameters for " + clazz.getName());
    }

}
