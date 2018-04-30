package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.ProblemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProblemDAO extends JpaRepository<Problem, Integer> {
	Page<Problem> findAllByType(ProblemType type, Pageable pageable);

	Page<Problem> findAllByCreatorIsNullOrCreator(User creator, Pageable pageable);

	@Query("SELECT p FROM Problem p WHERE (p.creator IS NULL OR p.creator = ?1) AND p.type = ?2")
	Page<Problem> findAllByCreatorIsNullOrCreatorAndType(User creator, ProblemType type, Pageable pageable);

	Problem findById(int id);

	Integer countByType(ProblemType type);
}
