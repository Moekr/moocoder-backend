package com.moekr.aes.web.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebHookDTO {
	@JsonProperty("checkout_sha")
	private String checkoutSha;
}
