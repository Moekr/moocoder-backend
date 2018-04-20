package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.ExaminationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExaminationDAO extends JpaRepository<Examination, Integer> {
	Examination findById(int id);

	List<Examination> findAllByStatus(ExaminationStatus status);

	Page<Examination> findAllByOwner(User owner, Pageable pageable);
}
