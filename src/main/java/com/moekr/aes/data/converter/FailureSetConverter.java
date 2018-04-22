package com.moekr.aes.data.converter;

import com.moekr.aes.data.entity.Record.Failure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.AttributeConverter;
import java.util.HashSet;
import java.util.Set;

public class FailureSetConverter implements AttributeConverter<Set<Failure>, String> {
	@Override
	public String convertToDatabaseColumn(Set<Failure> attribute) {
		JSONArray array = new JSONArray();
		for (Failure failure : attribute) {
			JSONObject object = new JSONObject();
			object.put("name", failure.getName());
			object.put("details", failure.getDetails());
			object.put("trace", failure.getTrace());
			array.put(object);
		}
		return array.toString();
	}

	@Override
	public Set<Failure> convertToEntityAttribute(String dbData) {
		Set<Failure> failures = new HashSet<>();
		JSONArray array = new JSONArray(dbData);
		for (Object object : array) {
			if (object instanceof JSONObject) {
				try {
					Failure failure = new Failure();
					failure.setName(((JSONObject) object).getString("name"));
					failure.setDetails(((JSONObject) object).getString("details"));
					failure.setTrace(((JSONObject) object).getString("trace"));
					failures.add(failure);
				} catch (JSONException ignore) { }
			}
		}
		return failures;
	}
}
