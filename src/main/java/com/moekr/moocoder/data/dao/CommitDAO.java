package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Commit;
import com.moekr.moocoder.data.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitDAO extends JpaRepository<Commit, String> {
	Commit findById(int commitId);

	Commit findFirstByResultOrderByIdDesc(Result result);

	List<Commit> findAllByResult_IdAndFinishedOrderByIdAsc(int resultId, boolean finished);
}
