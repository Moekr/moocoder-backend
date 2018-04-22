package com.moekr.aes.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"exam", "owner", "recordSet"})
@ToString(exclude = {"exam", "owner", "recordSet"})
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
	@Column(name = "deleted", columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
	private boolean deleted;

	@ManyToOne(targetEntity = Exam.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "exam", referencedColumnName = "id")
	private Exam exam;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", referencedColumnName = "id")
	private User owner;

	@OneToMany(targetEntity = Record.class, mappedBy = "result")
	@LazyCollection(LazyCollectionOption.EXTRA)
	@OrderBy("id desc")
	private List<Record> recordSet = new ArrayList<>();
}
