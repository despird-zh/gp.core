package com.gp.audit;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.util.CommonUtils;

/**
 * this class helps to convert AuditData object between json, as for different 
 * deployment environment, sometimes the auditdata need to be send to remote 
 * audit center to record so it will be converted into json to transfer.
 * 
 * @author gary diao
 * @version 0.1 2014-12-12
 * 
 **/
public class AuditConverter {
	
	static Logger LOGGER = LoggerFactory.getLogger(AuditConverter.class);
	
	static JsonFactory jsonFactory = new JsonFactory();

	/**
	 * Convert the object into a map 
	 **/
	public static Map<String,Object> beanToMap(Object beanObj){
		
		if(beanObj == null){  
            return null;  
        }   
		
		Map<String, Object> fieldMap = CommonUtils.JSON_MAPPER.convertValue(beanObj, new TypeReference<Map<String, Object>>(){});
		  
        return fieldMap;  
	}
	
	/**
	 * convert the predicates map to json string 
	 **/
	public static String mapToJson(Map<String, Object> predicates)throws IOException{
		
		if(predicates == null)
			return "{}";
		
		return CommonUtils.JSON_MAPPER.writeValueAsString(predicates);
	}
}
