package vn.com.lcx.common.ref;

public final class Ref<T> {

    private T val;

    private Ref(T val) {
        this.val = val;
    }

    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }

    public static <T> Ref<T> init(T val) {
        return new Ref<>(val);
    }

}
