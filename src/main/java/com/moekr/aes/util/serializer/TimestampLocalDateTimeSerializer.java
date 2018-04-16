package com.moekr.aes.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimestampLocalDateTimeSerializer extends LocalDateTimeSerializer {
	@Override
	public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
		g.writeNumber(value.atZone(ZoneId.systemDefault()).toEpochSecond());
	}
}
