package com.moekr.aes.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorResponse extends Response {
	@JsonProperty("msg")
	private String message;

	public ErrorResponse(int err, String message) {
		super(err);
		this.message = message;
	}
}
