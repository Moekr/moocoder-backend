package com.moekr.moocoder.logic.api.vo;

import com.offbytwo.jenkins.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class CoverageResult extends BaseModel {
	private Results results;

	public List<CoverageResultElement> getElements() {
		return results.getElements();
	}

	@Data
	@EqualsAndHashCode
	@ToString
	private static class Results {
		private List<CoverageResultElement> elements;
	}
}
