package com.moekr.aes.data.entity;

import com.moekr.aes.util.enums.ProblemType;
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
@EqualsAndHashCode(exclude = {"owner", "examinationSet"})
@ToString
@Entity
@Table(name = "ENTITY_PROBLEM")
@EntityListeners(AuditingEntityListener.class)
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ProblemType type;

	@Basic
	@Column(name = "description", columnDefinition = "TEXT NOT NULL")
	private String description;

	@Basic
	@Column(name = "public_files", columnDefinition = "TEXT NOT NULL")
	private String publicFiles = "[]";

	@Basic
	@Column(name = "protected_files", columnDefinition = "TEXT NOT NULL")
	private String protectedFiles = "[]";

	@Basic
	@Column(name = "private_files", columnDefinition = "TEXT NOT NULL")
	private String privateFiles = "[]";

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "deprecated", columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
	private Boolean deprecated = false;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", referencedColumnName = "id")
	private User owner;

	@ManyToMany(targetEntity = Examination.class)
	@JoinTable(name = "LINK_PROBLEM_EXAMINATION",
			joinColumns = @JoinColumn(name = "problem", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "examination", referencedColumnName = "id")
	)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Examination> examinationSet = new HashSet<>();

	public boolean isDeprecated() {
		return deprecated != null && deprecated;
	}
}
