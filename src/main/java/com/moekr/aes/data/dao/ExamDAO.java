package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamDAO extends JpaRepository<Exam, Integer> {
	Exam findById(int id);

	List<Exam> findAllByStatus(ExamStatus status);

	Page<Exam> findAllByCreator(User creator, Pageable pageable);
}
