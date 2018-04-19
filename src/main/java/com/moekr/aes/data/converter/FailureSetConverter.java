package com.moekr.aes.data.converter;

import com.google.common.base.Ascii;
import com.moekr.aes.data.entity.Record.Failure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FailureSetConverter implements AttributeConverter<Set<Failure>, String> {
	private static final int TEXT_MAX_LENGTH = 65500;
	private static final String TRUNCATE_INDICATOR = "[达到长度限制]";

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
		return formatFailure(array);
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

	private String formatFailure(JSONArray array) {
		String failure = array.toString();
		if (failure.length() < TEXT_MAX_LENGTH) {
			return failure;
		}
		List<JSONObject> failureList = new ArrayList<>();
		for (Object object : array) {
			if (object instanceof JSONObject) {
				failureList.add((JSONObject) object);
			}
		}
		List<Integer> traceLengthList = failureList.stream().map(o -> o.optString("trace")).map(String::length).sorted().collect(Collectors.toList());
		int totalLength = traceLengthList.stream().reduce((a, b) -> a + b).orElse(0);
		int targetLength = TEXT_MAX_LENGTH - (failure.length() - totalLength);
		int truncateLength = 0;
		for (int traceLength : traceLengthList) {
			int currentLength = traceLengthList.stream().map(a -> Math.min(a, traceLength)).reduce((a, b) -> a + b).orElse(0);
			if (currentLength <= targetLength) {
				truncateLength = currentLength;
			} else {
				break;
			}
		}
		int finalTruncateLength = truncateLength;
		failureList.forEach(f -> f.put("trace", Ascii.truncate(f.optString("trace"), finalTruncateLength, TRUNCATE_INDICATOR)));
		return array.toString();
	}
}
