package com.gp.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.gp.common.AccessPoint;
import com.gp.common.Principal;
import com.gp.exception.CoreException;
import com.gp.validate.ValidateMessage;
import com.gp.web.util.ExWebUtils;


/**
 * Define the basic method be used in Controller
 * 
 * @author gary diao
 * @version 0.1 2015-12-12 
 **/
public abstract class BaseController implements MessageSourceAware{

	static Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
	
	static PropertyNamingStrategyBase JSON_CASE_BASE = (PropertyNamingStrategyBase)PropertyNamingStrategy.SNAKE_CASE;
	
	public static ObjectMapper JACKSON_MAPPER = new ObjectMapper();
	
	public static ObjectMapper JACKSON_MAPPER_NON_NULL = new ObjectMapper();
	
	@Autowired
    protected HttpServletRequest request;
	
	static{
		
		JACKSON_MAPPER_NON_NULL.setSerializationInclusion(Include.NON_NULL);
		JACKSON_MAPPER_NON_NULL.setPropertyNamingStrategy(JSON_CASE_BASE);
		JACKSON_MAPPER.setPropertyNamingStrategy(JSON_CASE_BASE);
	}
	
	private MessageSource messageSource;
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	/**
	 * Get the access point object for the request
	 * @param request the request from client 
	 **/
	public static AccessPoint getAccessPoint(HttpServletRequest request){
		
		return ExWebUtils.getAccessPoint(request);
	}
	
	/**
	 * Get model view for json data, the model and view will accept multiple key-values.
	 **/
	public ModelAndView getJsonModelView(){
		
		return getJsonModelView(true); 		
	}
	
	/**
	 * Get model view for json data, the model and view will accept multiple key-values.
	 * @param includeNull true:ignore null in json; otherwize include null in json
	 **/
	public ModelAndView getJsonModelView(boolean includeNull){
		
		if(includeNull)
			return new ModelAndView(new MappingJackson2JsonView(JACKSON_MAPPER)); 	
		else
			return new ModelAndView(new MappingJackson2JsonView(JACKSON_MAPPER_NON_NULL)); 	
	}

	/**
	 * Get model view for json data, the attributeValue is the only data to be written.
	 **/
	public ModelAndView getJsonModelView(Object attributeValue){
		
		MappingSingleJsonView view = new MappingSingleJsonView();
		ModelAndView mav = new ModelAndView(view);
		mav.addObject(MappingSingleJsonView.SINGLE_KEY, attributeValue);
		
		return mav;
	}
	
	/**
	 * Get model view for jsp presentation 
	 **/
	public ModelAndView getJspModelView(String jspname){
		
		return new ModelAndView(jspname); 		
	}
	
	/**
	 * Get the request parameter, it's used for request with content type [application/x-www-form-urlencoded]. 
	 **/
	protected String readRequestParam(String parameterName) {
		
		String value =  request.getParameter(parameterName);

		return value;
	}
	
	/**
	 * Read the request parameters into certain data wrapper.
	 *
	 * @param datawrapper the data wrapper.
	 **/
	protected void readRequestParams(Object dataWrapper){
				
		ServletRequestDataBinder binder = new ServletRequestDataBinder(dataWrapper);
		HttpServletRequest origin = ExWebUtils.getNativeRequest(request, HttpServletRequest.class);
		binder.bind(origin); 
		
	}
	
	/**
	 * Read the request body into certain data wrapper class object.
	 *
	 * @param requestBody the data json string.
	 * @param clazz the class object
	 **/
	protected <T> T readRequestBody(String requestBody, Class<T> clazz){
				
		try {
			
			T result = JACKSON_MAPPER.readValue(requestBody, clazz);
			return result;
			
		} catch (IOException e) {
			LOGGER.debug("fail read the json data from request body", e);
		}
		return null;
		
	}
	
	/**
	 * Read the request JSON data, the request body expect to be JSON string
	 *
	 * @return Map<String, String> the request JSON
	 **/
	protected Map<String, String> readRequestJson(String requestBody){

		try {
			
			Map<String, String> map = JACKSON_MAPPER.readValue(requestBody, new TypeReference<Map<String, String>>(){});
			
			return map;
		} catch (IOException e) {
			LOGGER.debug("fail read the json data from request body", e);
		}
		return null;
	}
	
	/**
	 * Read the request parameter into object.
	 * e.g {"var1":"val1","num":2} into bean object
	 * 
	 * @param parameterName The name of request parameter
	 * @param clazz The class of target bean 
	 **/
	protected <T> T readRequestJson(String parameterName, Class<T> clazz){

		String value =  request.getParameter(parameterName);
		
		if(null == value)
			return null;
		else{
			try {
				return JACKSON_MAPPER.readValue(value, clazz);
			} catch (Exception e) {
				LOGGER.error("Fail to parse the request json",e);
				return null;
			} 
		}
	}
	
	/**
	 * Read the request parameter into object.
	 * e.g [{"var1":"val1","num":2},{"var1":"val1","num":2}] into bean object list
	 * 
	 * @param parameterName The name of request parameter
	 * @param clazz The class of target bean 
	 **/
	protected <T> List<T> readRequestJsonArray(String parameterName, Class<T> clazz){
		
		String value = request.getParameter(parameterName);
		
		if(null == value)
			return null;
		else{
			try {
				return JACKSON_MAPPER.readValue(value, TypeFactory.defaultInstance().constructCollectionType(List.class,  
						clazz));
			} catch (Exception e) {
				LOGGER.error("Fail to parse the request json",e);
				return null;
			} 
		}
	}
	
	/**
	 * weave the parameter with array values 
	 * @param parameter The parameter name
	 * @param values The array of values
	 * 
	 * @return the parameter string url encoded.
	 **/
	protected String weaveParameters(String parameter, String[] values) throws UnsupportedEncodingException{
		StringBuffer sbuf = new StringBuffer();
		if(null != values && values.length > 0){
			for(String value : values){
				sbuf.append(parameter).append("=")
					.append(UriUtils.encode(value, "UTF-8"))
					.append("&");
			}
			sbuf.setLength(sbuf.length()-1);
		}
		
		return sbuf.toString();
	}
	
	/**
	 * Get the principal from the security context 
	 **/
	public Principal getPrincipal(){
		
		return ExWebUtils.getPrincipal(request);
	}
	
	/**
	 * Get the message in different locale
	 * @param code the message code
	 * @param args the arguments
	 * @param locale the locale 
	 **/
	public String getMessage(String code, Object[] args){
		
		Principal principal = getPrincipal();
		Locale locale = (null == principal) ? Locale.getDefault() : principal.getLocale();
		return messageSource.getMessage(code, args, locale);
	}
	
	/**
	 * Get the message in different locale
	 * @param code the message code
	 * @param locale the locale 
	 **/
	public String getMessage(String code){
		Principal principal = getPrincipal();
		Locale locale = (null == principal) ? Locale.getDefault() : principal.getLocale();
		return messageSource.getMessage(code, new String[0], locale);
	}
	
	/**
	 * Wrap the core exception into an ActionResult
	 * @param ce the core exception  
	 * @return the ActionResult with exception
	 **/
	public static ActionResult wrapResult(CoreException ce){
		
		if(CollectionUtils.isNotEmpty(ce.getValidateMessages())){
			Map<String, String> msgmap = new HashMap<String, String>();
			for(ValidateMessage msg: ce.getValidateMessages()){

				msgmap.put(JSON_CASE_BASE.translate(msg.getProperty()), msg.getMessage());
			}
			return ActionResult.invalid(ce.getMessage(), msgmap);
		}else{
			
			return ActionResult.error(ce.getMessage());
		}
	}
}
