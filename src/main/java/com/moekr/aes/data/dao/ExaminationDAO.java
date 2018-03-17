package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExaminationDAO extends JpaRepository<Examination, Integer> {
	List<Examination> findAllByClosed(boolean closed);
}
