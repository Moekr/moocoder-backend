package com.moekr.aes.logic.service.impl;

import com.moekr.aes.util.enums.ProblemType;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@ToString(exclude = {"formattedContent", "description"})
public class FormattedProblemInfo {
	private byte[] formattedContent = new byte[0];
	private ProblemType type;
	private String name;
	private String description;
	private Set<String> publicFiles = new HashSet<>();
	private Set<String> protectedFiles = new HashSet<>();
	private Set<String> privateFiles = new HashSet<>();
}
