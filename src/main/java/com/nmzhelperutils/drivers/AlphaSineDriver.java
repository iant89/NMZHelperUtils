package com.nmzhelperutils.drivers;

public class AlphaSineDriver extends ValueDriver<Float>{

	private float minimum;
	private float maximum;
	private float value;
	private long duration;

	private boolean driveDown = false;

	private long nextStepTimer = -1;

	public static void main(String[] args) {

		// 30 = 1906
		// 31 = 1968, 1953
		AlphaSineDriver alphaDriver = new AlphaSineDriver(0.125f, 0.75f, 20);

		System.out.println("Step = " + alphaDriver.getStep());

		boolean minReached = false;
		boolean maxReached = false;

		long startTimer = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();

		float previousValue = Float.MAX_VALUE;
		while(true) {
			float value = alphaDriver.getValue();

			if(value == alphaDriver.getMaximum()) {
				maxReached = true;
			} else if(value == alphaDriver.getMinimum()) {
				minReached = true;
			}

			if(minReached && maxReached) {
				final long endTime = System.currentTimeMillis();
				double seconds = ((endTime - startTimer) / 1000.0);
				long milliseconds = endTime - startTimer;
				if(seconds < 1) {
					sb.append("Done, Took " + seconds + " Seconds\n\n");
				} else {
					sb.append("Done, Took " + milliseconds + " Milliseconds.");
				}

				break;
			}

			if(previousValue != Float.MAX_VALUE) {
				if(previousValue != value) {
					sb.append("" + value + "\n");
					previousValue = value;
				}
			} else {
				previousValue = value;
			}
		}

		System.out.println("" + sb.toString());

	}

	public AlphaSineDriver(float min, float max, long duration) {
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

		//float step = (getMaximum() - getMinimum()) / getDuration();
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