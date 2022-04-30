package com.github.iant89.ultimatenmz.drivers;

public class ConstantDriver extends ValueDriver {

    public ConstantDriver() {

    }

    public ConstantDriver(Number value) {
        this.value = value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value;
    }

    @Override
    public void calculateValue() {

    }

    @Override
    public Number getValue() {
        return this.value;
    }
}
