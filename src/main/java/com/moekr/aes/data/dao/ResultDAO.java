package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultDAO extends JpaRepository<Result, Integer> {
	Result findById(int id);

	List<Result> findAllByExam_Id(int examId);

	Result findByOwner_IdAndExam(int ownerId, Exam exam);

	Page<Result> findAllByOwner(User owner, Pageable pageable);
}
