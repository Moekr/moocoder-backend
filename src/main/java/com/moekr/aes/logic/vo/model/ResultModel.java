package com.moekr.aes.logic.vo.model;

import com.moekr.aes.data.entity.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
public class ResultModel {
	private Integer id;
	private Integer score;
	private List<RecordModel> recordList;

	public ResultModel(Result result) {
		BeanUtils.copyProperties(result, this);
		this.recordList = result.getRecordSet().stream()
				.map(RecordModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}
}
