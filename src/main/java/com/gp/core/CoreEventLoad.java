package com.gp.core;

import com.gp.common.AccessPoint;
import com.gp.common.GeneralContext;
import com.gp.disruptor.EventPayload;
import com.gp.disruptor.EventType;
import com.gp.info.InfoId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class wrap all the event fired during Core Running,
 * 
 * @author gary diao
 * @version 0.1 2015-12-10
 * 
 **/
public class CoreEventLoad extends EventPayload{

	/** the object id */
	private InfoId<?> objectId;

	/** the time stamp */
	private Long timestamp;

	/** operation time consuming */
	private Long elapse = 0L;

	/** the operation predicates */
	private Map<String, String> predicates = null;

	/** the execute message */
	private String message = null;

	/** access point */
	private AccessPoint accessPoint;

	/** the operation */
	private String operation;

	/** the operator of action */
	private String operator;

	/** workgroup id */
	private InfoId<Long> workgroupId = null;

	/** the execute flag */
	private String state = GeneralContext.ExecState.SUCCESS.toString();

	/**
	 * The constructor
	 *
	 * @param operation the operation
	 **/
	public CoreEventLoad() {
		this.timestamp = System.currentTimeMillis();
		this.predicates = new HashMap<>();
		this.setEventType(EventType.CORE);
	}
	
	/**
	 * Get the object id that identify the target of operation
	 **/
	public InfoId<?> getObjectId() {

		return objectId;
	}

	/**
	 * Set the object id of operation target data
	 **/
	public void setObjectId(InfoId<?> objectId) {

		this.objectId = objectId;
	}

	/**
	 * Get the operation name
	 * @return String the operation name
	 **/
	public String getOperation() {

		return operation;
	}

	/**
	 * Set the operation name
	 * @param  operation the name of operation
	 **/
	public void setOperation(String operation) {

		this.operation = operation;
	}

	/**
	 * Get the operator
	 **/
	public String getOperator() {
		return operator;
	}

	/**
	 * Set the operator
	 **/
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Get elapse time
	 **/
	public long getElapsedTime(){

		return elapse;

	}

	public void setElapsedTime(Long elapse){

		this.elapse = elapse;
	}

	/**
	 * Get the start timestamp
	 **/
	public Long getTimestamp(){

		return this.timestamp;
	}

	/**
	 * Set the end timestamp
	 **/
	public void setTimestamp(Long timestamp){

		this.timestamp = timestamp;
	}

	/**
	 * Add predicate to map
	 **/
	public void addPredicate(String predicateKey, String predicateValue) {

		predicates.put(predicateKey, predicateValue);
	}

	/**
	 * Add predicate to map
	 **/
	public void addPredicate(String predicateKey, Object predicateValue) {

		if(predicateValue == null)
			predicates.put(predicateKey, null);
		else
			predicates.put(predicateKey, predicateValue.toString());
	}

	/**
	 * Remove predicate via verb name
	 **/
	public void removePredicate(String predicateKey) {

		predicates.remove(predicateKey);
	}

	/**
	 * Get predicate map
	 **/
	public Map<String, String> getPredicates() {
		return predicates;
	}

	public void addPredicates(Map<String, String> predicates) {

		if(predicates != null)
			this.predicates.putAll(predicates);

	}

	public void clearPredicates(){
		if(predicates == null)
			return;
		this.predicates.clear();
	}

	public AccessPoint getAccessPoint() {
		return accessPoint;
	}

	public void setAccessPoint(AccessPoint accessPoint) {
		this.accessPoint = accessPoint;
	}


	public InfoId<Long> getWorkgroupId() {
		return workgroupId;
	}

	public void setWorkgroupId(InfoId<Long> workgroupId) {
		this.workgroupId = workgroupId;
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
