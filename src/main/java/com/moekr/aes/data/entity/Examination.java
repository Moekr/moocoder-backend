package com.moekr.aes.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"user", "problem", "resultSet"})
@ToString
@Entity
@Table(name = "ENTITY_EXAMINATION")
public class Examination {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "name")
	private String name;

	@Basic
	@Column(name = "project")
	private String project;

	@Basic
	@Column(name = "closed")
	private Boolean closed;

	@Basic
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Basic
	@Column(name = "end_at")
	private LocalDateTime endAt;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user", referencedColumnName = "id")
	private User user;

	@ManyToOne(targetEntity = Problem.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "problem", referencedColumnName = "id")
	private Problem problem;

	@OneToMany(targetEntity = Result.class, mappedBy = "examination")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Result> resultSet = new HashSet<>();
}
