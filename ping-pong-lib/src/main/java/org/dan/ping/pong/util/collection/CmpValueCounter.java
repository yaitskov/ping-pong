package org.dan.ping.pong.util.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CmpValueCounter<T> {
    private T value;
    private int repeats;

    public CmpValueCounter() {
    }

    @JsonIgnore
    public CmpValueCounter(T value, int repeats) {
        this.value = value;
        this.repeats = repeats;
    }

    public int hashCode() {
        return value.hashCode() * repeats;
    }

    public boolean equals(Object o) {
        if (o instanceof CmpValueCounter) {
            CmpValueCounter<T> co = (CmpValueCounter<T>) o;
            return value.equals(co.value)
                    && repeats == co.repeats;
        }
        return false;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public CmpValueCounter<T> increment() {
        ++repeats;
        return this;
    }
}
