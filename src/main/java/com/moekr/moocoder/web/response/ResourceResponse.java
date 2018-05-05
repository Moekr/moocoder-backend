package com.moekr.moocoder.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceResponse extends EmptyResponse {
	@JsonProperty("res")
	private Object resource;

	public ResourceResponse(Object resource) {
		this.resource = resource;
	}
}
