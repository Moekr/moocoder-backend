package com.moekr.moocoder.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"exam", "owner", "commitList"})
@ToString(exclude = {"exam", "owner", "commitList"})
@Entity
@Table(name = "ENTITY_RESULT")
public class Result {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "score", columnDefinition = "INT(11) NOT NULL DEFAULT 0")
	private Integer score = 0;

	@Basic
	@Column(name = "last_commit_at")
	private LocalDateTime lastCommitAt;

	@Basic
	@Column(name = "deleted", columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
	private boolean deleted;

	@ManyToOne(targetEntity = Exam.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "exam", referencedColumnName = "id")
	private Exam exam;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", referencedColumnName = "id")
	private User owner;

	@OneToMany(targetEntity = Commit.class, mappedBy = "result", cascade = CascadeType.REMOVE)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@OrderBy("created_at DESC")
	private List<Commit> commitList = new ArrayList<>();
}
