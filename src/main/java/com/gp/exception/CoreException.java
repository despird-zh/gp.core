package com.gp.exception;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gp.core.DictionaryFacade;
import com.gp.exception.BaseException;
import com.gp.validate.ValidateMessage;

/**
 * CoreException wrap the exception occurs in core package.
 * It support the database side message pattern from dictionary table.
 * 
 * @author gary diao
 * @version 0.1 2015-1-2
 * 
 **/
public class CoreException extends BaseException{

	private static final long serialVersionUID = 6149095030747094149L;
	
	private Set<ValidateMessage> messageSet = null;

	/**
	 * Constructor with error code and parameters
	 **/
	public CoreException(String errorcode,Object ...param){
		this(Locale.getDefault(),errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public CoreException(String errorcode, Throwable cause,Object ...param) {
        this(Locale.getDefault(), errorcode, cause, param);
    }

	/**
	 * Constructor with error code and parameters
	 **/
	public CoreException(Locale locale, String errorcode, Object... param) {
		super(locale, errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public CoreException(Locale locale, String errorcode, Throwable cause,Object ...param) {
        super(locale, errorcode, cause, param);
    }

	/**
	 * Constructor with cause
	 **/
    public CoreException(Throwable cause) {
        super(cause);
        this.locale = Locale.getDefault();
    }
    	
    @Override
	protected String findMessage(Locale locale, String code, Object ... param){

    	String messagePattern = getMessagePattern(locale, code);
		if(StringUtils.equals(messagePattern, code)){
			return super.findMessage(locale, code, param);
		}
		return MessageFormat.format(messagePattern, param);
	}

    /**
     * get the message pattern from database.
     **/
    private String getMessagePattern(Locale locale, String code){
		
		String messagePattern = DictionaryFacade.findMessagePattern(locale, code);
		return StringUtils.isBlank(messagePattern) ? code : messagePattern;
    }
    
    /**
     * get the message pattern from database.
     **/
    private String getPropertyName(Locale locale, String code){
		
		String messagePattern = DictionaryFacade.findPropertyName(locale, code);
		return StringUtils.isBlank(messagePattern) ? code : messagePattern;
    }
    
    /**
     * Add validate message set to exception 
     **/
    public void addValidateMessages(Set<ValidateMessage> messageSet){
    	
    	// Translate property name as per locale.
    	for(ValidateMessage message : messageSet){
    		String prop = getPropertyName(this.locale, message.getProperty());
    		message.setProperty(prop);
    	}
    	
    	if(null == this.messageSet){
    		this.messageSet = messageSet;
    	}else{
    		this.messageSet.addAll(messageSet);
    	}
    }
    
    /**
     * Add validate message with property key and message key
     **/
    public void addValidateMessage(String propkey, String messagekey){
    	String prop = getPropertyName(this.locale, propkey);
    	String msg = getMessagePattern(this.locale, messagekey);
    	
    	if(null == this.messageSet){
    		this.messageSet = new HashSet<ValidateMessage>();
    	}
    	this.messageSet.add(new ValidateMessage(prop, msg));
    }
    
    /**
     * Add validate message with property key and message key
     **/
    public void addValidateMessage(ValidateMessage vmesg){
    	
    	String prop = getMessagePattern(this.locale, vmesg.getProperty());
    	vmesg.setProperty(prop);
    	if(null == this.messageSet){
    		this.messageSet = new HashSet<ValidateMessage>();
    	}
    	this.messageSet.add(vmesg);
    }

	/**
	 * Get the validate message set
	 **/
    public Set<ValidateMessage> getValidateMessages(){
    	
    	return this.messageSet;
    }
}
