package com.gp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Application Context Helper Class facilitate to get the bean manually.
 * 
 * @author gdiao
 * @version 0.2 2016-1-12
 **/
public class AppContextHelper implements ApplicationContextAware {

	Logger LOGGER = LoggerFactory.getLogger(AppContextHelper.class);
	
	private static ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		applicationContext = context;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getSpringBean(String beanname, Class<T> clazz){
		
		if(null == applicationContext) return null;
		
		Object bean = applicationContext.getBean(beanname);
		return (T) bean;
	}
	
	public static <T> T getSpringBean(Class<T> clazz){
		
		if(null == applicationContext) return null;

		return applicationContext.getBean(clazz);
	}
}
