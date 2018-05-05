package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.ProblemType;
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
