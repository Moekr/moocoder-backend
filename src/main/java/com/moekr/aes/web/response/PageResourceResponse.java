package com.moekr.aes.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResourceResponse extends ResourceResponse {
	@JsonProperty("page")
	private PageInfo pageInfo;

	public PageResourceResponse(Page page) {
		super(page.getContent());
		this.pageInfo = new PageInfo(page);
	}

	@Data
	private static class PageInfo {
		private int page;
		private int limit;
		private int size;
		private long total;

		PageInfo(Page page) {
			this.page = page.getNumber() + 1;
			this.limit = page.getSize();
			this.size = page.getContent().size();
			this.total = page.getTotalElements();
		}
	}
}
