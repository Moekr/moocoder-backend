package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordDAO extends JpaRepository<Record, Integer> {
	@Query(value = "SELECT rec.* FROM ENTITY_RECORD AS rec, ENTITY_RESULT AS res WHERE rec.result = res.id AND res.user = ?1", nativeQuery = true)
	List<Record> findAllByUser(int userId);
}
