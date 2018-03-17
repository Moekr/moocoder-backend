package com.moekr.aes.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = "result")
@ToString
@Entity
@Table(name = "ENTITY_RECORD")
public class Record {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Basic
	@Column(name = "compiled")
	private Boolean compiled;

	@Basic
	@Column(name = "score")
	private Integer score;

	@Basic
	@Column(name = "record", columnDefinition = "TEXT")
	private String pass;

	@Basic
	@Column(name = "fail", columnDefinition = "TEXT")
	private String fail;

	@ManyToOne(targetEntity = Result.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "result", referencedColumnName = "id")
	private Result result;
}
