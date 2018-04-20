package com.moekr.aes.data.dao;

import com.moekr.aes.data.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecordDAO extends JpaRepository<Record, Integer> {
	Record findById(int id);

	@Query(value = "SELECT rec.* FROM ENTITY_RECORD AS rec, ENTITY_RESULT AS res WHERE rec.result = res.id AND res.owner = ?1", nativeQuery = true)
	List<Record> findAllByUser(int userId);

	@Query(value = "SELECT * FROM ENTITY_RECORD AS rec WHERE rec.result = ?1 AND rec.number = ?2", nativeQuery = true)
	Optional<Record> findByResultIdAndNumber(int resultId, int number);
}
