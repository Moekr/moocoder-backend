package com.moekr.aes.data.converter;

import org.json.JSONArray;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringListConverter implements AttributeConverter<List<String>, String> {
	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		attribute = attribute.stream().filter(Objects::nonNull).collect(Collectors.toList());
		return new JSONArray(attribute).toString();
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		JSONArray array = new JSONArray(dbData);
		List<String> list = new ArrayList<>();
		for (Object object : array) {
			if (object instanceof String) {
				list.add((String) object);
			}
		}
		return list;
	}
}
