package com.github.iant89.ultimatenmz.drivers;

public class StepDriver extends ValueDriver<Integer> {

    private int minimum;
    private int maximum;
    private int value;
    private long delay;

    private boolean driveDown = false;

    private long nextStepTimer = -1;

    public StepDriver(int min, int max, long delay) {
        this.minimum = min;
        this.maximum = max;
        this.delay = delay;
        this.value = min;

        calculateValue();
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return this.delay;
    }


    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    public void calculateValue() {
        if(nextStepTimer == -1) {
            nextStepTimer = System.currentTimeMillis() + getDelay();
        } else {
            if(System.currentTimeMillis() >= nextStepTimer) {
                nextStepTimer = System.currentTimeMillis() + getDelay();
            } else {
                return;
            }
        }

        if(driveDown) {
            value -= 1;

            if(value <= getMinimum()) {
                value = getMinimum();
                driveDown = false;
            }
        } else {
            value += 1;

            if(value >= getMaximum()) {
                value = getMaximum();
                driveDown = true;
            }
        }
    }

    public Integer getValue() {
        calculateValue();

        return value;
    }
}
