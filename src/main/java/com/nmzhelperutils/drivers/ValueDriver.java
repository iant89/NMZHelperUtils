package com.nmzhelperutils.drivers;

public abstract class ValueDriver<T extends Number> {

    private T value;

    public abstract void setValue(T value);

    public abstract void calculateValue();

    public abstract T getValue();
}
