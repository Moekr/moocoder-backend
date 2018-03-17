package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.util.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemDAO extends JpaRepository<Problem, Integer> {
	Integer countByLanguage(Language language);
}
