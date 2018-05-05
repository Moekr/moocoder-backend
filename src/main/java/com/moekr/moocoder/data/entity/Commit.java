package com.moekr.moocoder.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"result", "records"})
@ToString(exclude = {"result", "records"})
@Entity
@Table(name = "ENTITY_COMMIT")
@EntityListeners(AuditingEntityListener.class)
public class Commit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "hash", nullable = false)
	private String hash;

	@Basic
	@Column(name = "finished", columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
	private boolean finished;

	@Basic
	@Column(name = "score", nullable = false)
	private Integer score = 0;

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@ManyToOne(targetEntity = Result.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "result", referencedColumnName = "id")
	private Result result;

	@OneToMany(targetEntity = Record.class, mappedBy = "commit", cascade = CascadeType.REMOVE)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Record> records = new HashSet<>();
}
