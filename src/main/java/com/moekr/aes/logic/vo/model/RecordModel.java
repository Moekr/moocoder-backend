package com.moekr.aes.logic.vo.model;

import com.moekr.aes.data.entity.Record;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONArray;
import org.springframework.beans.BeanUtils;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
public class RecordModel {
	private Integer id;
	private Long createdAt;
	private Boolean compiled;
	private Integer score;
	private Integer examinationId;
	private String examinationName;
	private Set<String> passSet;
	private Set<Fail> failSet;

	public RecordModel(Record record) {
		BeanUtils.copyProperties(record, this);
		this.createdAt = record.getCreatedAt().toEpochSecond(ZoneOffset.ofHours(8));
		this.examinationId = record.getResult().getExamination().getId();
		this.examinationName = record.getResult().getExamination().getName();
		JSONArray pass = new JSONArray(record.getPass());
		this.passSet = pass.toList().stream()
				.filter(o -> o instanceof String)
				.map(o -> (String) o)
				.collect(Collectors.toSet());
		JSONArray fail = new JSONArray(record.getFail());
		this.failSet = fail.toList().stream()
				.filter(o -> o instanceof Map)
				.map(o -> (Map) o)
				.map(this::parse)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Fail parse(Map map) {
		Fail fail = new Fail();
		fail.name = (String) map.get("name");
		fail.details = (String) map.get("details");
		fail.trace = (String) map.get("trace");
		if (ObjectUtils.allNotNull(fail.name, fail.details, fail.trace)) return fail;
		return null;
	}

	@Data
	@EqualsAndHashCode
	@ToString
	public class Fail {
		private String name;
		private String details;
		private String trace;
	}
}
