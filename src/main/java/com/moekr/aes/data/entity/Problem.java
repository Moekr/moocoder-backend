package com.moekr.aes.data.entity;

import com.moekr.aes.data.converter.StringSetConverter;
import com.moekr.aes.util.enums.ProblemType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"creator", "examSet"})
@ToString(exclude = {"creator", "examSet"})
@Entity
@Table(name = "ENTITY_PROBLEM")
@Where(clause = "deprecated = 0")
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
	@Column(name = "public_files", columnDefinition = "JSON NOT NULL")
	@Convert(converter = StringSetConverter.class)
	private Set<String> publicFiles = new HashSet<>();

	@Basic
	@Column(name = "protected_files", columnDefinition = "JSON NOT NULL")
	@Convert(converter = StringSetConverter.class)
	private Set<String> protectedFiles = new HashSet<>();

	@Basic
	@Column(name = "private_files", columnDefinition = "JSON NOT NULL")
	@Convert(converter = StringSetConverter.class)
	private Set<String> privateFiles = new HashSet<>();

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;

	@Basic
	@Column(name = "deprecated", columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
	private boolean deprecated;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "creator", referencedColumnName = "id")
	private User creator;

	@ManyToMany(targetEntity = Exam.class, cascade = CascadeType.DETACH)
	@JoinTable(name = "LINK_PROBLEM_EXAM",
			joinColumns = @JoinColumn(name = "problem", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "exam", referencedColumnName = "id")
	)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Exam> examSet = new HashSet<>();

	public String getUniqueName() {
		return (id + "-" + name).toLowerCase();
	}

	public String getImageName() {
		return getUniqueName();
	}

	public String getImageTag() {
		return modifiedAt == null ? null : String.valueOf(modifiedAt.atZone(ZoneId.systemDefault()).toEpochSecond());
	}
}
