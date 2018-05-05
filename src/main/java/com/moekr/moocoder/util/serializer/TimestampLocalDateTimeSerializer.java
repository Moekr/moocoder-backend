package com.moekr.moocoder.util.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimestampLocalDateTimeSerializer extends LocalDateTimeSerializer {
	@Override
	protected LocalDateTimeSerializer withFormat(Boolean useTimestamp, DateTimeFormatter f, JsonFormat.Shape shape) {
		return this;
	}

	@Override
	public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
		g.writeNumber(value.atZone(ZoneId.systemDefault()).toEpochSecond());
	}

	@Override
	public void serializeWithType(LocalDateTime value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		serialize(value, g, provider);
	}
}
