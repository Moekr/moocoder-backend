package com.moekr.moocoder.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmptyResponse implements Response {
	@JsonProperty("err")
	private int error;

	public EmptyResponse() {
		this.error = 0;
	}

	protected EmptyResponse(int error) {
		this.error = error;
	}
}
