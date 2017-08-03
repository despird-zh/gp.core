package com.gp.audit;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.collections.map.TransformedMap;
import org.apache.commons.lang.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

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

	@SuppressWarnings("unchecked")
	public static Map<String,String> beanToMap(Object beanObj){
		
		if(beanObj == null){  
            return null;  
        }          
        Map<String, String> map = null;
        if(beanObj instanceof Map){
        	map = new HashMap<String, String>();
        	map = TransformedMap.decorate(map, TransformerUtils.stringValueTransformer(), TransformerUtils.stringValueTransformer());
        	map.putAll((Map)beanObj);
        	return map;
        }
        
        try {  
        	map = new HashMap<String, String>();  
            BeanInfo beanInfo = Introspector.getBeanInfo(beanObj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                if (!key.equals("class")) {  
                    // get the property
                    Method getter = property.getReadMethod();  
                    Object value = getter.invoke(beanObj);  
  
                    map.put(key, String.valueOf(value));  
                }  
  
            }  
        } catch (Exception e) {  
            LOGGER.error("fail convert bean to map",e);  
        }  
  
        return map;  
	}
	
	/**
	 * convert the predicates map to json string 
	 **/
	public static MutableObject mapToJson(Map<String, String> predicates)throws IOException{
		
		if(predicates == null)
			return new MutableObject("{}");
		
		ByteArrayOutputStream byteAryStream = new ByteArrayOutputStream();		
		JsonGenerator jsonGen = jsonFactory.createGenerator(byteAryStream);
		jsonGen.writeStartObject();
		
		for(Map.Entry<String, String> entry:predicates.entrySet())
			jsonGen.writeStringField(entry.getKey(), entry.getValue());
		
		jsonGen.writeEndObject();
		jsonGen.close();
		
		return new MutableObject(byteAryStream.toString());
	}
}
