package com.moekr.aes.util.editors;

import org.apache.commons.lang3.StringUtils;

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
		if (StringUtils.isNumeric(s)) {
			int value = Integer.valueOf(s);
			setValue(Math.max(min, Math.min(max, value)));
		} else {
			super.setAsText(s);
		}
	}
}
