package com.moekr.aes.logic.api.vo;

import com.offbytwo.jenkins.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class CoberturaResult extends BaseModel {
	private Results results;

	public List<CoberturaElement> getElements() {
		return results.getElements();
	}

	@Data
	@EqualsAndHashCode
	@ToString
	private static class Results {
		private List<CoberturaElement> elements;
	}
}
