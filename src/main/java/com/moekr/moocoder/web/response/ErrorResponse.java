package com.moekr.moocoder.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends EmptyResponse {
	@JsonProperty("msg")
	private String message;

	public ErrorResponse(int err, String message) {
		super(err);
		this.message = message;
	}
}
