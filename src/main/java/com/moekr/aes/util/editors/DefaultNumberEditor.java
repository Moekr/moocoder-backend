package com.moekr.aes.util.editors;

import com.sun.beans.editors.IntegerEditor;
import org.apache.commons.lang3.StringUtils;

public class DefaultNumberEditor extends IntegerEditor {
	private final int defaultValue;

	public DefaultNumberEditor(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		if (StringUtils.isNumeric(s)) {
			setValue(Integer.valueOf(s));
		} else {
			setValue(defaultValue);
		}
	}
}
