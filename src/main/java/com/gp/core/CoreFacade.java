package com.gp.core;

import java.util.Locale;

import com.gp.common.AccessPoint;
import com.gp.common.GPrincipal;
import com.gp.common.JwtPayload;
import com.gp.dao.info.AuditInfo;
import com.gp.dao.info.SysOptionInfo;
import com.gp.dao.info.TokenInfo;
import com.gp.exception.CoreException;
import com.gp.info.InfoId;

public interface CoreFacade {

	/**
	 * Persist the audit information 
	 * 
	 * @param operaudit the audit information of operation
	 **/
	InfoId<Long> persistAudit(AuditInfo operaudit) throws CoreException;
	
	/**
	 * Get the message pattern from dictionary 
	 * 
	 * @param locale the locale setting
	 * @param dictKey the key of dictionary entry, eg. excp.find.workgroup 
	 **/
	String findMessagePattern(Locale locale, String dictKey);
	
	/**
	 * Find the bean property name, user hibernate validator to check the data.
	 * but the property name is bean property, here convert the bean property name 
	 * to localized string. eg. sourceId = 来源ID
	 * 
	 * @param locale the locale setting
	 * @param dictKey the key of dictionary entry
	 **/
	String findPropertyName(Locale locale, String dictKey);
	
	/**
	 * find the system options by group key 
	 **/
	SysOptionInfo findSystemOption(AccessPoint accesspoint,
				GPrincipal principal,
				String optionKey)throws CoreException;
	
	/**
	 * Find the jwt token by token Id 
	 **/
	TokenInfo findToken(AccessPoint accesspoint,
			InfoId<Long> tokenId) throws CoreException;
	
	/**
	 * Find the principal by userId, account and type
	 **/
	GPrincipal findPrincipal(AccessPoint accesspoint,
			InfoId<Long> userId,
			String account, String type) throws CoreException;
	
	/**
	 * Reissue a new token by JWT payload, there will be a token per subject & audience.
	 * 
	 * @param payload the JWT payload
	 * @return String the JWT token string 
	 **/
	String reissueToken(AccessPoint accesspoint,GPrincipal principal, JwtPayload payload) throws CoreException;
	
	/**
	 * Remove the token by token Id 
	 **/
	boolean removeToken(AccessPoint accesspoint,GPrincipal principal, InfoId<Long> tokenId) throws CoreException;
	
	/**
	 * authenticate the password
	 **/
	Boolean authenticate(AccessPoint accesspoint, GPrincipal principal,  String password)throws CoreException;
	
	/**
	 * create a new token by JWT payload, there will be a token per subject & audience.
	 * 
	 * @param payload the JWT payload
	 * @return String the JWT token string 
	 **/
	String newToken(AccessPoint accesspoint, JwtPayload payload) 	throws CoreException;
}
