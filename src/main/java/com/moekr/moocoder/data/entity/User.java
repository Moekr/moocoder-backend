package com.moekr.moocoder.data.entity;

import com.moekr.moocoder.util.enums.UserRole;
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
@EqualsAndHashCode(exclude = {"problemSet", "examSet", "resultSet"})
@ToString(exclude = {"problemSet", "examSet", "resultSet"})
@Entity
@Table(name = "ENTITY_USER", indexes = {@Index(columnList = "username"), @Index(columnList = "email")})
@EntityListeners(AuditingEntityListener.class)
public class User {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "username", nullable = false)
	private String username;

	@Basic
	@Column(name = "password", nullable = false)
	private String password;

	@Basic
	@Column(name = "email", nullable = false)
	private String email;

	@Basic
	@Column(name = "namespace", nullable = false)
	private Integer namespace;

	@Basic
	@Column(name = "token", nullable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@OneToMany(targetEntity = Problem.class, mappedBy = "creator", cascade = CascadeType.REMOVE)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Problem> problemSet = new HashSet<>();

	@OneToMany(targetEntity = Exam.class, mappedBy = "creator", cascade = CascadeType.REMOVE)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Exam> examSet = new HashSet<>();

	@OneToMany(targetEntity = Result.class, mappedBy = "owner", cascade = CascadeType.REMOVE)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Result> resultSet = new HashSet<>();
}
