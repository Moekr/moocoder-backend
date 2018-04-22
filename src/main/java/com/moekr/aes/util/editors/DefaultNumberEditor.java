package com.moekr.aes.util.editors;

import com.sun.beans.editors.IntegerEditor;

public class DefaultNumberEditor extends IntegerEditor {
	private final int defaultValue;

	public DefaultNumberEditor(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		try {
			setValue(Integer.valueOf(s));
		} catch (NumberFormatException e) {
			setValue(defaultValue);
		}
	}
}
