package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.util.enums.ProblemType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemDAO extends JpaRepository<Problem, Integer> {
	Problem findById(int id);

	Integer countByType(ProblemType type);
}
