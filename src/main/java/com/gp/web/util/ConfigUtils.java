package com.gp.web.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.common.AccessPoint;
import com.gp.common.GeneralConfig;
import com.gp.common.GroupUsers;
import com.gp.common.ServiceContext;
import com.gp.common.SystemOptions;
import com.gp.core.CoreEngine;
import com.gp.exception.CoreException;
import com.gp.dao.info.SysOptionInfo;


/**
 * Detect configuration setting in following sequence.
 * <ol>
 * 	<li>Find in META-INF/core-configuration file</li>
 *  <li>Find in core-configuration file</li>
 *  <li>Find in gp_sys_options</li>
 * </ol>
 * This class be called in SystemService constructor to initial the service instance.
 *
 * @author gary diao
 * @version 0.1 2015-1-1
 * 
 **/
public class ConfigUtils {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);
		
	/**
	 * get the system option value
	 * 
	 * @param key the option key
	 * @return String the option value
	 **/
	public static String getSystemOption(String key){
		
		String value = GeneralConfig.getString(key);
		
		if(StringUtils.isBlank(value)){
			try {
				SysOptionInfo soi = CoreEngine.getCoreFacade().findSystemOption(GroupUsers.PSEUDO_USER, key);
				
				value = soi != null ? soi.getOptionValue() : null;
			
			} catch (CoreException e) {
				LOGGER.error("fail to fetch the system option info", e );
			}
		}
		return value;		
	}

}
