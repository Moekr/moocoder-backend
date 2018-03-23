package com.moekr.aes.data.entity;

import com.moekr.aes.util.enums.Language;
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
@EqualsAndHashCode(exclude = "user")
@ToString
@Entity
@Table(name = "ENTITY_PROBLEM")
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "name")
	private String name;

	@Enumerated
	@Column(name = "language")
	private Language language;

	@Basic
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Basic
	@Column(name = "file")
	private String file;

	@Basic
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "deprecated")
	private Boolean deprecated;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user", referencedColumnName = "id")
	private User user;

	@OneToMany(targetEntity = Examination.class, mappedBy = "problem")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Examination> examinationSet = new HashSet<>();
}
