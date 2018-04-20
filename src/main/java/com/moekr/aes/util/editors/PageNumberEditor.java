package com.moekr.aes.util.editors;

public class PageNumberEditor extends DefaultNumberEditor {
	private static final int DEFAULT_PAGE_NUMBER = 1;

	public PageNumberEditor() {
		super(DEFAULT_PAGE_NUMBER);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Integer) {
			super.setValue((Integer) value - 1);
		} else {
			super.setValue(value);
		}
	}
}
