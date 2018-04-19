package com.moekr.aes.util.editors;

import com.sun.beans.editors.IntegerEditor;
import org.apache.commons.lang3.StringUtils;

public class PageNumberEditor extends IntegerEditor {
	private static final int DEFAULT_PAGE_NUMBER = 0;

	@Override
	public void setAsText(String s) {
		if (StringUtils.isNumeric(s)) {
			setValue(Math.max(Integer.valueOf(s) - 1, DEFAULT_PAGE_NUMBER));
		} else {
			setValue(DEFAULT_PAGE_NUMBER);
		}
	}
}
