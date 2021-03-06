package com.gp.audit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StopWatch;

import com.gp.info.InfoId;

/**
 * AuditVerb hold the step level detail information
 * 
 * @author despird
 **/
public class AuditTracer {
	
	/** the time stamp */
	private Long timestamp;	

	StopWatch stopWatch = new StopWatch();
	
	/** the verb */
	private String verb = null;
	/** the target data EntryKey */
	private InfoId<?> objectId = null;
	/** predicateMap */
	private Map<String, Object> predicateMap = null;

	/**
	 * Default constructor 
	 **/
	public AuditTracer(){
		this.timestamp = System.currentTimeMillis();
		stopWatch.start();
		predicateMap = new HashMap<String, Object>();
	}
	
	/**
	 * Constructor with verb 
	 **/
	public AuditTracer(String verb){
		this.verb = verb;
		this.timestamp = System.currentTimeMillis();
		stopWatch.start();
		predicateMap = new HashMap<String, Object>();
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

		return this.stopWatch.getTotalTimeMillis();

	}
	
	/**
	 * Set the elapse time, use the current time as end point.
	 **/
	public void setElapsedTime(){

		stopWatch.stop();
	}

	/**
	 * Get time stamp 
	 **/
	public Long getTimestamp(){
		
		return this.timestamp;
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
	public Map<String, Object> getPredicates() {
		return predicateMap;
	}

	/**
	 * Add audit predicate map 
	 * @param predicatemap the map of predicates
	 **/
	public void addPredicates(Map<String, Object> predicatemap) {

		if(predicatemap != null)
			this.predicateMap.putAll(predicatemap);
		
	}
	
	/**
	 * Clear the predicates map 
	 **/
	public void clearPredicates(){
		if(predicateMap == null)
			return;
		this.predicateMap.clear();
	}
}
