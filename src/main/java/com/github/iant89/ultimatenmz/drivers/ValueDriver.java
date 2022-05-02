package com.github.iant89.ultimatenmz.drivers;

public abstract class ValueDriver<T extends Number> {

    protected T value;

    public abstract void setValue(T value);

    protected abstract void calculateValue();

    public abstract T getValue();
}
