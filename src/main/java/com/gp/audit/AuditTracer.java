package com.gp.audit;

import java.util.HashMap;
import java.util.Map;

import com.gp.info.InfoId;

/**
 * AuditVerb hold the step level detail information
 * 
 * @author despird
 **/
public class AuditTracer {
	
	/** the time stamp */
	private Long timestamp;	
	/** operation time consuming */
	private Long elapse = 0L;	

	/** the verb */
	private String verb = null;
	/** the target data EntryKey */
	private InfoId<?> objectId = null;
	/** predicateMap */
	private Map<String, String> predicateMap = null;

	/**
	 * Default constructor 
	 **/
	public AuditTracer(){
		this.timestamp = System.currentTimeMillis();
		predicateMap = new HashMap<String, String>();
	}
	
	/**
	 * Constructor with verb 
	 **/
	public AuditTracer(String verb){
		this.verb = verb;
		this.timestamp = System.currentTimeMillis();
		predicateMap = new HashMap<String, String>();
	}

	/**
	 * Constructor with verb and target 
	 **/
	public AuditTracer(String verb, InfoId<?> objectId){
		this(verb);
		this.objectId = objectId;
	}
	
	/**
	 * Get target data 
	 **/
	public InfoId<?> getObjectId() {
		return objectId;
	}

	/**
	 * Set target 
	 **/
	public void setObjectId(InfoId<?> objectId) {
		this.objectId = objectId;
	}
	
	/**
	 * Get verb 
	 **/
	public String getVerb(){
		
		return this.verb;
	}

	/**
	 * Set the verb
	 **/
	public void setVerb(String verb) {
		this.verb = verb;
	}
	
	/**
	 * Get elapse time 
	 **/
	public long getElapsedTime(){

		return elapse;

	}
	
	/**
	 * Set the elapse time, use the current time as end point.
	 **/
	public void setElapsedTime(){
		
		long currTimestamp = System.currentTimeMillis();
		this.elapse = currTimestamp - this.timestamp;
	}
	
	/**
	 * Set the elapse time 
	 **/
	public void setElapsedTime(Long elapse){
		
		this.elapse = elapse;
	}

	/**
	 * Get time stamp 
	 **/
	public Long getTimestamp(){
		
		return this.timestamp;
	}
	
	/**
	 * Set time stamp 
	 **/
	public void setTimestamp(Long timestamp){
		
		this.timestamp = timestamp;
	}

	/**
	 * Add predicate to map 
	 **/
	public void addPredicate(String predicateName, String predicateValue) {

		predicateMap.put(predicateName, predicateValue);
	}
	
	/**
	 * Add predicate to map 
	 **/
	public void addPredicate(String predicateName, Object predicateValue) {
		
		if(predicateValue == null)
			predicateMap.put(predicateName, null);
		else
			predicateMap.put(predicateName, predicateValue.toString());
	}
	
	/**
	 * Remove predicate via verb name
	 **/
	public void removePredicate(String predicatename) {
		
		predicateMap.remove(predicatename);
	}
	
	/**
	 * Get predicate map 
	 **/
	public Map<String, String> getPredicates() {
		return predicateMap;
	}

	public void addPredicates(Map<String, String> predicatemap) {

		if(predicatemap != null)
			this.predicateMap.putAll(predicatemap);
		
	}
	
	public void clearPredicates(){
		if(predicateMap == null)
			return;
		this.predicateMap.clear();
	}
}
