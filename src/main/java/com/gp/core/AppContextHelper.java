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
	
	// the application context
	private static ApplicationContext applicationContext;

	/**
	 * Set the application context 
	 **/
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		applicationContext = context;
	}

	/**
	 * Get the application context 
	 **/
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	/**
	 * Get the spring bean from context 
	 **/
	public static <T> T getSpringBean(String beanname, Class<T> clazz){
		
		if(null == applicationContext) return null;
		
		T bean = applicationContext.getBean(beanname, clazz);
		return  bean;
	}
	
	/**
	 * Get the spring bean 
	 * @param clazz the bean of class
	 **/
	public static <T> T getSpringBean(Class<T> clazz){
		
		if(null == applicationContext) return null;

		return applicationContext.getBean(clazz);
	}
	
	/**
	 * Autowire the existing bean 
	 **/
	public static void autowireBean(Object existingBean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(existingBean);
	}
}
