package com.moekr.moocoder.util.serializer;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeSerializer extends LocalDateTimeSerializer {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	public CustomLocalDateTimeSerializer() {
		super(FORMATTER);
	}
}
