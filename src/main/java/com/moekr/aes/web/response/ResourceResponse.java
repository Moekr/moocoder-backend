package com.moekr.aes.web.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceResponse extends Response {
	private Object res;

	public ResourceResponse(Object res) {
		this.res = res;
	}
}
