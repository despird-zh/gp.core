package com.gp.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.AbstractLocaleResolver;
import com.gp.common.Principal;
import com.gp.web.util.ExWebUtils;

/**
 * Parse the locale information out of request
 * 
 * @author diaogc
 * @version 0.1 2016-11-12
 * 
 **/
public class PrincipalLocaleResolver extends AbstractLocaleResolver{

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		
		Principal principal = ExWebUtils.getPrincipal(request);
		if(null != principal){
			setDefaultLocale(principal.getLocale());
		}else{
			setDefaultLocale(request.getLocale());
		}
        return getDefaultLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		 super.setDefaultLocale(locale);
	}

}
