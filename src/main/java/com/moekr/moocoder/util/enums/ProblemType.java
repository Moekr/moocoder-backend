package com.moekr.moocoder.util.enums;

import com.moekr.moocoder.util.problem.evaluator.*;
import com.moekr.moocoder.util.problem.helper.*;
import lombok.Getter;

@Getter
public enum ProblemType {
	/**
	 * 普通Java题目，统计测试用例通过情况
	 */
	JAVA("Java", "JAVA", "TEST",
			new JavaHelper(), new BasicEvaluator()),
	/**
	 * 普通Python题目，统计测试用例通过情况
	 */
	PYTHON("Python", "PYTHON", "TEST",
			new PythonHelper(), new PythonEvaluator()),
	/**
	 * Java测试覆盖率题目，统计测试覆盖率
	 */
	JAVA_COVERAGE("Java Coverage", "JAVA", "COVERAGE",
			new JavaCoverageHelper(), new BasicCoverageEvaluator()),
	/**
	 * Python测试覆盖率题目，统计测试覆盖率
	 */
	PYTHON_COVERAGE("Python Coverage", "PYTHON", "COVERAGE",
			new PythonCoverageHelper(), new BasicCoverageEvaluator()),
	/**
	 * Java变异覆盖题目，统计变异覆盖率
	 */
	JAVA_MUTATION("Java Mutation", "JAVA", "MUTATION",
			new JavaMutationHelper(), new BasicMutationEvaluator());

	private final String name;
	private final String language;
	private final String target;
	private final ProblemHelper helper;
	private final Evaluator evaluator;

	ProblemType(String name, String language, String target, ProblemHelper helper, Evaluator evaluator) {
		this.name = name;
		this.language = language;
		this.target = target;
		this.helper = helper;
		this.evaluator = evaluator;
	}

	@Override
	public String toString() {
		return name;
	}
}
