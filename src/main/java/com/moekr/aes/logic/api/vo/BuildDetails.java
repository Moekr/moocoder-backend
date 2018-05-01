package com.moekr.aes.logic.api.vo;

import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.TestResult;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "consoleOutput")
public class BuildDetails {
	private int number;
	private long duration;
	private BuildResult buildResult;
	private String consoleOutput;
	private TestResult testResult;
	private CoberturaResult coberturaResult;
}
