package me.xdark.reflectionhooks.api;

public class NonDirectReference<T> {

    private T value;

    public NonDirectReference(T referent) {
        this.value = referent;
    }

    /**
     * @retun value of reference
     */
    public T get() {
        return value;
    }

    /**
     * Set new value of reference
     *
     * @param value new value
     */
    public void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
