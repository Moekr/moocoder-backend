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
@EqualsAndHashCode(exclude = {"examination", "owner", "recordSet"})
@ToString
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
	private Boolean deleted = false;

	@ManyToOne(targetEntity = Examination.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "examination", referencedColumnName = "id")
	private Examination examination;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", referencedColumnName = "id")
	private User owner;

	@OneToMany(targetEntity = Record.class, mappedBy = "result")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<Record> recordSet = new HashSet<>();

	public boolean isDeleted() {
		return deleted != null && deleted;
	}
}
