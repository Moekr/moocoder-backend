package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Commit;
import com.moekr.moocoder.data.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitDAO extends JpaRepository<Commit, String> {
	Commit findById(int commitId);

	Commit findFirstByResultOrderByIdDesc(Result result);

	Commit findFirstByResultAndFinishedOrderByIdAsc(Result result, boolean finished);

	default Commit findFirstUnfinishedByResult(Result result) {
		return findFirstByResultAndFinishedOrderByIdAsc(result, false);
	}
}
