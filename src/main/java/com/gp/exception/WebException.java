package com.gp.exception;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.gp.exception.BaseException;

public class WebException extends BaseException{

	private static final long serialVersionUID = 1L;

	private static Map<Locale, ResourceBundle> web_bundles = new HashMap<Locale, ResourceBundle>();

	/**
	 * Constructor with error code and parameters
	 **/
	public WebException(String errorcode,Object ...param){
		this(Locale.getDefault(),errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public WebException(String errorcode, Throwable cause,Object ...param) {
        this(Locale.getDefault(), errorcode, cause, param);
    }

	/**
	 * Constructor with error code and parameters
	 **/
	public WebException(Locale locale, String errorcode, Object... param) {
		super(errorcode, param);
		this.message = findMessage(locale, errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public WebException(Locale locale, String errorcode, Throwable cause,Object ...param) {
        super(errorcode, cause);
        this.message = findMessage(locale,errorcode, param);
    }

	/**
	 * Constructor with cause
	 **/
    public WebException(Throwable cause) {
        super(cause);
    }
    	
    @Override
	protected String findMessage(Locale locale, String errorcode,Object ... param){
		
		ResourceBundle rb = web_bundles.get(locale);
		if(rb == null){
			rb = loadResourceBundle(locale, WebException.class);
			web_bundles.put(locale, rb);
		}
		String messagePattern = null;
		if(rb == null || !rb.containsKey(errorcode)) {
			matched = false;
			return super.findMessage(locale, errorcode, param);
		} else{
			matched = true;
			messagePattern = rb.getString(errorcode);
		}

		return MessageFormat.format(messagePattern, param);
	}

}
