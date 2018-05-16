package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.util.enums.BuildStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface RecordDAO extends JpaRepository<Record, Integer> {
	Record findById(int id);

	Set<Record> findAllByStatus(BuildStatus status);

	Record findByCommit_Result_IdAndNumber(int resultId, int number);

	default Record findByResultIdAndBuildNumber(int resultId, int buildNumber) {
		return findByCommit_Result_IdAndNumber(resultId, buildNumber);
	}

	Record findFirstByCommit_ResultAndCommit_FinishedAndProblemOrderByIdDesc(Result result, boolean finished, Problem problem);

	default Record findLastBuiltByResultAndProblem(Result result, Problem problem) {
		return findFirstByCommit_ResultAndCommit_FinishedAndProblemOrderByIdDesc(result, true, problem);
	}
}
