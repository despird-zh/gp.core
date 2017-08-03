package com.gp.web;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.gp.core.DictionaryFacade;

public class DatabaseMessageSource extends AbstractMessageSource implements ResourceLoaderAware{

	@SuppressWarnings("unused")
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {

		String messagePattern = DictionaryFacade.findMessagePattern(locale, code);
		messagePattern = StringUtils.isBlank(messagePattern) ? code : messagePattern;
		
		MessageFormat result = createMessageFormat(messagePattern, locale);
		return result;
	}

}
