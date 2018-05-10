package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordDAO extends JpaRepository<Record, Integer> {
	Record findById(int id);

	Record findByCommit_Result_IdAndNumber(int resultId, int number);

	Record findFirstByCommit_ResultAndCommit_FinishedAndProblemOrderByIdDesc(Result result, boolean finished, Problem problem);

	default Record findLastBuiltByResultAndProblem(Result result, Problem problem) {
		return findFirstByCommit_ResultAndCommit_FinishedAndProblemOrderByIdDesc(result, true, problem);
	}
}
