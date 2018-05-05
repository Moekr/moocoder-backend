package com.moekr.moocoder.util.editors;

public class RangeNumberEditor extends DefaultNumberEditor {
	private final int min;
	private final int max;

	public RangeNumberEditor(int min, int max, int defaultValue) {
		super(defaultValue);
		this.min = min;
		this.max = max;
	}

	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		try {
			int value = Integer.valueOf(s);
			setValue(Math.max(min, Math.min(max, value)));
		} catch (NumberFormatException e) {
			super.setAsText(s);
		}
	}
}
