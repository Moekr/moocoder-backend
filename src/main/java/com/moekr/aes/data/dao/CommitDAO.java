package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitDAO extends JpaRepository<Commit, String> {
	Commit findFirstByResult_IdAndFinishedOrderByIdAsc(int resultId, boolean finished);
}
