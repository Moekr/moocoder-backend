package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResultDAO extends JpaRepository<Result, Integer> {
	Result findById(int id);

	@Query(value = "SELECT r.* FROM ENTITY_RESULT AS r, ENTITY_USER AS u WHERE r.exam = ?1 AND r.owner = u.id AND u.role = 'STUDENT'", nativeQuery = true)
	List<Result> findAllByExamination(int examinationId);

	Result findByOwner_IdAndExam(int ownerId, Exam exam);

	Page<Result> findAllByOwner(User owner, Pageable pageable);
}
