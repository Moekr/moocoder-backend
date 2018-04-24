package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitDAO extends JpaRepository<Commit, String> {
	List<Commit> findAllByResult_IdAndFinishedOrderByIdAsc(int resultId, boolean finished);
}
