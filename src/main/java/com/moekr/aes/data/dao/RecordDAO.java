package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordDAO extends JpaRepository<Record, Integer> {
	Record findById(int id);

	Record findByCommit_Result_IdAndNumber(int resultId, int number);
}
