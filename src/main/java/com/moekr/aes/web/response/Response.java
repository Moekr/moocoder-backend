package com.moekr.aes.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {
	@JsonProperty("err")
	private int error;

	public Response() {
		this.error = 0;
	}

	protected Response(int error) {
		this.error = error;
	}
}
