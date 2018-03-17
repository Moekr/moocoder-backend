package com.moekr.aes.data.entity;

import com.moekr.aes.util.enums.Role;
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
@EqualsAndHashCode(exclude = "resultSet")
@ToString
@Entity
@Table(name = "ENTITY_USER", indexes = {@Index(columnList = "username"), @Index(columnList = "email")})
public class User {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "username")
	private String username;

	@Basic
	@Column(name = "password")
	private String password;

	@Basic
	@Column(name = "email")
	private String email;

	@Basic
	@Column(name = "namespace")
	private Integer namespace;

	@Basic
	@Column(name = "token")
	private String token;

	@Enumerated
	@Column(name = "role")
	private Role role;

	@Basic
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToMany(targetEntity = Problem.class, mappedBy = "user")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Problem> problemSet = new HashSet<>();

	@OneToMany(targetEntity = Examination.class, mappedBy = "user")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Examination> examinationSet = new HashSet<>();

	@OneToMany(targetEntity = Result.class, mappedBy = "user")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Result> resultSet = new HashSet<>();
}
