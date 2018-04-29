package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.ProblemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemDAO extends JpaRepository<Problem, Integer> {
	Page<Problem> findAllByType(ProblemType type, Pageable pageable);

	Page<Problem> findAllByCreator(User creator, Pageable pageable);

	Page<Problem> findAllByCreatorAndType(User creator, ProblemType type, Pageable pageable);

	Problem findById(int id);

	Integer countByType(ProblemType type);
}
