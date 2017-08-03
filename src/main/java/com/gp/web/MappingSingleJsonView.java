package com.gp.web;

import java.util.Map;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.view.json.AbstractJackson2View;

public class MappingSingleJsonView extends AbstractJackson2View {

	/**
	 * Default content type: "application/json".
	 * Overridable through {@link #setContentType}.
	 */
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	
	public static final String SINGLE_KEY = "_single_key";
	
	private String modelKey = SINGLE_KEY;
	
	protected MappingSingleJsonView() {
		super(Jackson2ObjectMapperBuilder.json().build(), DEFAULT_CONTENT_TYPE);;
	}

	@Override
	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}

	@Override
	protected Object filterModel(Map<String, Object> model) {
		Object result = model.get(this.modelKey);
		return result;
	}
}
