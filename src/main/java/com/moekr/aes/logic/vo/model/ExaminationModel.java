package com.moekr.aes.logic.vo.model;

import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode
@ToString
public class ExaminationModel {
	private Integer id;
	private String name;
	private String project;
	private Status status;
	private Long startAt;
	private Long endAt;
	private Long createdAt;
	private Language language;
	private Integer resultCount;

	public ExaminationModel(Examination examination) {
		BeanUtils.copyProperties(examination, this);
		this.status = status(examination.getStartAt(), examination.getEndAt());
		this.startAt = examination.getStartAt().atZone(ZoneId.systemDefault()).toEpochSecond();
		this.endAt = examination.getEndAt().atZone(ZoneId.systemDefault()).toEpochSecond();
		this.createdAt = examination.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond();
		this.language = examination.getProblem().getLanguage();
		this.resultCount = examination.getResultSet().size();
	}

	private Status status(LocalDateTime startAt, LocalDateTime endAt) {
		LocalDateTime now = LocalDateTime.now();
		if (startAt.isAfter(now)) {
			return Status.BEFORE;
		} else if (endAt.isBefore(now)) {
			return Status.AFTER;
		} else {
			return Status.ONGOING;
		}
	}
}
