package com.moekr.aes.data.entity;

import com.moekr.aes.util.enums.ExaminationStatus;
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
@EqualsAndHashCode(exclude = {"creator", "problemSet", "resultSet"})
@ToString(exclude = {"creator", "problemSet", "resultSet"})
@Entity
@Table(name = "ENTITY_EXAM")
@EntityListeners(AuditingEntityListener.class)
public class Exam {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "uuid", nullable = false)
	private String uuid;

	@Basic
	@Column(name = "name", nullable = false)
	private String name;

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Basic
	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Basic
	@Column(name = "version", columnDefinition = "INT(11) NOT NULL DEFAULT 0")
	private Integer version = 0;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "status", columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 'PREPARING'")
	private ExaminationStatus status = ExaminationStatus.PREPARING;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "creator", referencedColumnName = "id")
	private User creator;

	@ManyToMany(targetEntity = Problem.class)
	@JoinTable(name = "LINK_PROBLEM_EXAM",
			joinColumns = @JoinColumn(name = "exam", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "problem", referencedColumnName = "id")
	)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Problem> problemSet = new HashSet<>();

	@OneToMany(targetEntity = Result.class, mappedBy = "exam")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Result> resultSet = new HashSet<>();

	public void setStatus(ExaminationStatus status) {
		if (status == ExaminationStatus.READY || status == ExaminationStatus.FINISHED) {
			throw new IllegalArgumentException("READY 与 FINISHED 状态不应持久化！");
		}
		this.status = status;
	}
}
