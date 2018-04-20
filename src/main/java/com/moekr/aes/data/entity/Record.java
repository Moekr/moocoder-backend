package com.moekr.aes.data.entity;

import com.moekr.aes.data.converter.FailureSetConverter;
import com.moekr.aes.util.enums.BuildStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "result")
@ToString(exclude = "result")
@Entity
@Table(name = "ENTITY_RECORD")
@EntityListeners(AuditingEntityListener.class)
public class Record {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Basic
	@Column(name = "number", nullable = false)
	private Integer number;

	@Basic
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 'WAITING'")
	private BuildStatus status = BuildStatus.WAITING;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "console_output", columnDefinition = "TEXT NOT NULL")
	private String consoleOutput = "";

	@Basic
	@Column(name = "score", columnDefinition = "INT(11) NOT NULL DEFAULT 0")
	private Integer score = 0;

	@Basic
	@Column(name = "failures", columnDefinition = "JSON NOT NULL")
	@Convert(converter = FailureSetConverter.class)
	private Set<Failure> failures = new HashSet<>();

	@ManyToOne(targetEntity = Result.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "result", referencedColumnName = "id")
	private Result result;

	@Data
	public static class Failure {
		private String name;
		private String details;
		private String trace;
	}
}
