package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultDAO extends JpaRepository<Result, Integer> {
	Result findById(int id);

	Result findByOwnerAndExam(User owner, Exam exam);

	List<Result> findAllByExam(Exam exam);

	List<Result> findAllByExamAndOwnerIsNot(Exam exam, User owner);
}
