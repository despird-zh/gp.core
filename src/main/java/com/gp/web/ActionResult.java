package com.gp.web;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;

import com.gp.core.CoreConstants;
/**
 * ActionResult Wrap the result of JSON data.
 * 
 * @author diaogc
 * @version 0.1 2014-12-10
 * 
 **/
public class ActionResult{
	
	/** the meta information of result */
	private Meta meta = null;

	/** the data holder of result */
	private Object data = null;

	/**
	 * Build a new failure state result with message
	 * 
	 * @param message the message string
	 * @return ActionResult the action result
	 **/
    public static ActionResult failure(String message) {
    		ActionResult ref = new ActionResult();
        ref.meta = new Meta(CoreConstants.FAIL, message);
        return ref;
    }
    
	/**
	 * Build a new error state result with message
	 * 
	 * @param message the message string
	 * @return ActionResult the action result
	 **/
    public static ActionResult error(String message) {
    	ActionResult ref = new ActionResult();
        ref.meta = new Meta(CoreConstants.ERROR, message);
        return ref;
    }
    
	/**
	 * Build a new success state result with message
	 * 
	 * @param message the message string
	 * @return ActionResult the action result
	 **/
    public static ActionResult success(String message) {
    	ActionResult ref = new ActionResult();
        ref.meta = new Meta(CoreConstants.SUCCESS, message);
        return ref;
    }
    
	/**
	 * Build a new success state result with message
	 * 
	 * @param message the message string
	 * @return ActionResult the action result
	 **/
    public static ActionResult invalid(String message, Map<String, String> msgMap) {
    		ActionResult ref = new ActionResult();
        ref.meta = new Meta(CoreConstants.FAIL, message);
        ref.meta.setCode(CoreConstants.CODE_INVALID);
        ref.setData(msgMap);
        
        return ref;
    }
    
    /**
     * Get meta information
     * @return Meta the meta  
     **/
	public Meta getMeta() {
		return meta;
	}

	/**
	 * Set meta information 
	 **/
	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	/**
	 * Get the data information 
	 **/
	public Object getData() {
		return data;
	}

	/**
	 * Set the data information 
	 **/
	public void setData(Object data) {
		this.data = data;
	}	
	
	/**
	 * Check the result is success or not
	 **/
	public boolean isSuccess() {
		
		return StringUtils.equals(CoreConstants.SUCCESS, this.meta.state);
	}
	
	/**
	 * Check the result is fail or not
	 **/
	public boolean isFailure() {
			
		return StringUtils.equals(CoreConstants.FAIL, this.meta.state);
	}
	
	/**
	 * Check the result is error or not
	 **/
	public boolean isError() {
		
		return StringUtils.equals(CoreConstants.ERROR, this.meta.state);
	}

	/**
	 * Convert the action result into a map, it to be easy to append to ModelAndView
	 **/
	public Map<String, Object> asMap(){
		
		Map<String, Object> map = new HashMap<String,Object>();
		map.put(CoreConstants.MODEL_KEY_META, this.meta);
		map.put(CoreConstants.MODEL_KEY_DATA, data);
		
		return map;
	}
	
	/**
	 * Meta class keep the state, error code and message. 
	 **/
    public static class Meta {

    	/** the state of meta */
        private String state;
        /** the code of meta */
        private String code;
        /** the message of meta */
        private String message;

        /**
         * Create Meta with state 
         **/
        public Meta(String state) {
            this.state = state;
        }

        /**
         * Create Meata with state and message 
         **/
        public Meta(String state, String message) {
            this.state = state;
            this.message = message;
        }

        /**
         * Get the state 
         **/
        public String getState() {
            return state;
        }

        /**
         * Get the message 
         **/
        public String getMessage() {
            return message;
        }

        /**
         * Get the code 
         **/
		public String getCode() {
			return code;
		}

		/**
		 * Set the code 
		 **/
		public void setCode(String code) {
			this.code = code;
		}
    }
}
