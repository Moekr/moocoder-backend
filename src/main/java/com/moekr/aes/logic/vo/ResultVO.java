package com.moekr.aes.logic.vo;

import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.BuildStatus;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResultVO {
	private Integer id;
	private Integer score;
	private boolean deleted;
	private NestedExaminationVO examination;
	private NestedUserVO owner;
	private List<NestedRecordVO> recordList;

	public ResultVO(Result result) {
		BeanUtils.copyProperties(result, this);
		this.examination = new NestedExaminationVO(result.getExamination());
		this.owner = new NestedUserVO(result.getOwner());
		this.recordList = result.getRecordSet().stream().map(NestedRecordVO::new).collect(Collectors.toList());
	}

	@Data
	private static class NestedExaminationVO {
		private Integer id;
		private String name;

		NestedExaminationVO(Examination examination) {
			BeanUtils.copyProperties(examination, this);
		}
	}

	@Data
	private static class NestedUserVO {
		private Integer id;
		private String username;

		NestedUserVO(User user) {
			BeanUtils.copyProperties(user, this);
		}
	}

	@Data
	private static class NestedRecordVO {
		private Integer id;
		private LocalDateTime createdAt;
		private BuildStatus status;
		private Integer score;

		NestedRecordVO(Record record) {
			BeanUtils.copyProperties(record, this);
		}
	}
}
