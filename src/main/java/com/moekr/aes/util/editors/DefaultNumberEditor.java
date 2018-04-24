package com.moekr.aes.util.editors;

import java.beans.PropertyEditorSupport;

public class DefaultNumberEditor extends PropertyEditorSupport {
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

	@Override
	public String getJavaInitializationString() {
		Object var1 = this.getValue();
		return var1 != null ? var1.toString() : "null";
	}
}
