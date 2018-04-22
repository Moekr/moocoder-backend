package com.moekr.aes.util.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimestampLocalDateDeserializer extends JsonDeserializer<LocalDateTime> {
	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
		Long epochSecond = null;
		if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
			epochSecond = p.getLongValue();
		} else if (p.hasToken(JsonToken.VALUE_STRING)) {
			String value = p.getText().trim();
			try {
				epochSecond = Long.valueOf(value);
			} catch (NumberFormatException e) {
				epochSecond = null;
			}
		}
		if (epochSecond != null) {
			return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
		}
		return null;
	}
}
