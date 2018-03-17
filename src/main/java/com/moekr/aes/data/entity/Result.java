package com.moekr.aes.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"examination", "user", "recordSet"})
@ToString
@Entity
@Table(name = "ENTITY_RESULT")
public class Result {
	@Id
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "score")
	private Integer score;

	@ManyToOne(targetEntity = Examination.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "examination", referencedColumnName = "id")
	private Examination examination;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user", referencedColumnName = "id")
	private User user;

	@OneToMany(targetEntity = Record.class, mappedBy = "result")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Record> recordSet = new HashSet<>();
}
