package com.github.iant89.ultimatenmz.drivers;

public class SineDriver extends ValueDriver<Float> {

	private float minimum;
	private float maximum;
	private float value;
	private long duration;

	private boolean driveDown = false;

	private long nextStepTimer = -1;

	public SineDriver(float min, float max, long duration) {
		this.minimum = min;
		this.maximum = max;
		this.duration = duration;
		this.value = min;

		calculateValue();
	}

	public float getMinimum() {
		return minimum;
	}

	public void setMinimum(float minimum) {
		this.minimum = minimum;
	}

	public float getMaximum() {
		return maximum;
	}

	public void setMaximum(float maximum) {
		this.maximum = maximum;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDuration() {
		return this.duration;
	}

	public float getStep() {
		return (getMaximum() - getMinimum()) / getDuration();
	}

	@Override
	public void setValue(Float value) {
		this.value = value;
	}

	public void calculateValue() {
		if(nextStepTimer == -1) {
			nextStepTimer = System.currentTimeMillis() + getDuration();
		} else {
			if(System.currentTimeMillis() >= nextStepTimer) {
				nextStepTimer = System.currentTimeMillis() + getDuration();
			} else {
				return;
			}
		}

		float step = (getMaximum() - getMinimum()) / getDuration();

		if(driveDown) {
			value -= step;

			if(value <= getMinimum()) {
				value = getMinimum();
				driveDown = false;
			}
		} else {
			value += step;

			if(value >= getMaximum()) {
				value = getMaximum();
				driveDown = true;
			}
		}
	}

	public Float getValue() {
		calculateValue();

		return value;
	}
}