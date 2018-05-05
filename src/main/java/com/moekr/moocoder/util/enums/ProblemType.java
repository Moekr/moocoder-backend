package com.moekr.moocoder.util.enums;

import com.moekr.moocoder.util.problem.evaluator.BasicCoverageEvaluator;
import com.moekr.moocoder.util.problem.evaluator.BasicEvaluator;
import com.moekr.moocoder.util.problem.evaluator.Evaluator;
import com.moekr.moocoder.util.problem.evaluator.PythonEvaluator;
import com.moekr.moocoder.util.problem.helper.*;
import lombok.Getter;

@Getter
public enum ProblemType {
	/**
	 * 普通Java题目，统计测试用例通过情况
	 */
	JAVA("Java", new JavaHelper(), new BasicEvaluator()),
	/**
	 * 普通Python题目，统计测试用例通过情况
	 */
	PYTHON("Python", new PythonHelper(), new PythonEvaluator()),
	/**
	 * Java测试覆盖率题目，统计测试覆盖率
	 */
	JAVA_COVERAGE("Java Coverage", new JavaCoverageHelper(), new BasicCoverageEvaluator()),
	/**
	 * Python测试覆盖率题目，统计测试覆盖率
	 */
	PYTHON_COVERAGE("Python Coverage", new PythonCoverageHelper(), new BasicCoverageEvaluator());

	private final String name;
	private final ProblemHelper helper;
	private final Evaluator evaluator;

	ProblemType(String name, ProblemHelper helper, Evaluator evaluator) {
		this.name = name;
		this.helper = helper;
		this.evaluator = evaluator;
	}

	@Override
	public String toString() {
		return name;
	}
}
