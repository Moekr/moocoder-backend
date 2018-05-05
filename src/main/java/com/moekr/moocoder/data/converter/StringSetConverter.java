package com.moekr.moocoder.data.converter;

import org.json.JSONArray;

import javax.persistence.AttributeConverter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StringSetConverter implements AttributeConverter<Set<String>, String> {
	@Override
	public String convertToDatabaseColumn(Set<String> attribute) {
		attribute = attribute.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		return new JSONArray(attribute).toString();
	}

	@Override
	public Set<String> convertToEntityAttribute(String dbData) {
		JSONArray array = new JSONArray(dbData);
		Set<String> set = new HashSet<>();
		for (Object object : array) {
			if (object instanceof String) {
				set.add((String) object);
			}
		}
		return set;
	}
}
