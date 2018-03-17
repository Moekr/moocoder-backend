package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResultDAO extends JpaRepository<Result, Integer> {

	@Query(value = "SELECT * FROM ENTITY_RESULT AS r WHERE r.user = ?1 AND r.examination = ?2", nativeQuery = true)
	Result findOne(int userId, int examinationId);

	@Query(value = "SELECT r.* FROM ENTITY_RESULT AS r, ENTITY_USER AS u WHERE r.examination = ?1 AND r.user = u.id AND u.role = 0", nativeQuery = true)
	List<Result> findAllByExamination(int examinationId);
}
