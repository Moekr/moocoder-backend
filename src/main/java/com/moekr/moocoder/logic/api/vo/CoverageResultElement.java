package com.moekr.moocoder.logic.api.vo;

import lombok.Data;

@Data
public class CoverageResultElement {
	private String name;
	private int numerator;
	private int denominator;
	private int ratio;
}
