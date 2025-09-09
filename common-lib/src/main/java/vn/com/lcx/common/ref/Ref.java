package vn.com.lcx.common.ref;

/**
 * A generic reference wrapper class that allows mutable access to a value.
 * <p>
 * This class can be useful when you need to simulate pass-by-reference semantics
 * in Java, where method parameters are normally passed by value. By wrapping
 * a value inside a {@code Ref}, changes to the value can be observed outside
 * the scope of the method.
 * </p>
 *
 * <pre>{@code
 * Ref<String> ref = Ref.init("Hello");
 * ref.setVal("World");
 * System.out.println(ref.getVal()); // prints "World"
 * }</pre>
 *
 * @param <T> the type of the referenced value
 */
public final class Ref<T> {

    private T val;

    /**
     * Creates a new reference with the given value.
     *
     * @param val the initial value to store in this reference
     */
    private Ref(T val) {
        this.val = val;
    }

    /**
     * Returns the current value stored in this reference.
     *
     * @return the current value, may be {@code null}
     */
    public T getVal() {
        return val;
    }

    /**
     * Updates the value stored in this reference.
     *
     * @param val the new value to set (can be {@code null})
     */
    public void setVal(T val) {
        this.val = val;
    }

    /**
     * Creates a new {@code Ref} with the specified initial value.
     *
     * @param val the initial value to wrap
     * @param <T> the type of the value
     * @return a new {@code Ref} instance containing the provided value
     */
    public static <T> Ref<T> init(T val) {
        return new Ref<>(val);
    }

    /**
     * Creates a new {@code Ref} with a {@code null} initial value.
     *
     * @param <T> the type of the value
     * @return a new {@code Ref} instance with {@code null} as its value
     */
    public static <T> Ref<T> init() {
        return new Ref<>(null);
    }
}
