package berries.servermod.tcm.util;

public class SingleTimeSetField<T> {
    private T value;
    private boolean set = false;

    public SingleTimeSetField() {}

    public void set(T value) {
        if (!set) {
            this.value = value;
        } else {
            throw new IllegalStateException();
        }
        set = true;
    }

    public T get() {
        return value;
    }
}
