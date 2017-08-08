package com.gp.core;

import java.util.Map;

import com.gp.audit.AuditTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.audit.AuditConverter;
import com.gp.common.AccessPoint;
import com.gp.common.Principal;
import com.gp.common.ServiceContext;
import com.gp.disruptor.EventDispatcher;
import com.gp.info.InfoId;

/**
 * this class extends service context, collect necessary data of one operation.
 * these operation data is stored in a AuditData.
 * 
 * @author gary diao
 * @version 0.1 2014-12-12
 **/
public class CoreServiceContext extends ServiceContext{
	
	static Logger LOGGER = LoggerFactory.getLogger(CoreServiceContext.class);
	
	/**
	 * audit data holder 
	 **/
	//private CoreEventLoad coreload = null;

	/**
	 * AuditState
	 **/
	private ExecState execstate = ExecState.UNKNOWN;
	
	/**
	 * Audit verb tracker 
	 **/
	private AuditTracer verbTracer = null;
	
	/**
	 * The access point 
	 **/
	private AccessPoint accessPoint = null;
	
	private String message;
	
	/**
	 * constructor with principal 
	 **/
	public CoreServiceContext(Principal principal){
		
		super(principal);
		setAuditable(true);

		verbTracer = new AuditTracer();
	}
	
	/**
	 * constructor with principal and accesspoint 
	 **/
	public CoreServiceContext(Principal principal, AccessPoint accesspoint){
		
		this(principal);

		this.accessPoint = accesspoint;
	}
	
	/**
	 * set access point information
	 **/
	@Override
	public void setAccessPoint(String client, String host, String app, String version){
		
		this.accessPoint = new AccessPoint(client, host, app, version);
	}
	
	@Override
	public void beginOperation(String subject,String operation, InfoId<?> object, Object predicate){

		verbTracer.setVerb(operation);
		verbTracer.setObjectId(object);

		// convert object to map object so as to save as json
		Map<String,Object> predicates = AuditConverter.beanToMap(predicate);		
		verbTracer.addPredicates(predicates);

	}
	
	@Override
	public void addOperationPredicate(String predicateKey, Object predicate){

		verbTracer.addPredicate(predicateKey, predicate.toString());
	}
	
	@Override
	public void addOperationPredicates(Object predicate){

		Map<String,Object> predicates = AuditConverter.beanToMap(predicate);
		// set operation primary verb predicates
		verbTracer.addPredicates(predicates);
	}
	
	@Override 
	public void setOperationObject(InfoId<?> objectId){
		
		verbTracer.setObjectId(objectId);
	}
	
	/**
	 * endOperation will only be called once. 
	 * 
	 * @param state the execution state
	 * @param message the message
	 **/
	@Override
	public void endOperation(ExecState state, String message){

		// already be set to other non-success state, don't change it to success.
		if(execstate != ExecState.UNKNOWN && state == ExecState.SUCCESS)
			return ;
		
		verbTracer.setElapsedTime();
		execstate = state;
		this.message = message;

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A> A getOperationData(Class<A> clazz){

		if(clazz.isAssignableFrom(CoreEventLoad.class)){
			
			CoreEventLoad coreEvent = new CoreEventLoad();
			coreEvent.setWorkgroupId(this.getWorkgroupId());
			coreEvent.setMessage(this.message);
			coreEvent.setAccessPoint(this.accessPoint);
			coreEvent.setOperation(verbTracer.getVerb());
			coreEvent.setObjectId(verbTracer.getObjectId());
			coreEvent.setState(this.execstate.name());
			coreEvent.setOperator(this.getPrincipal().getAccount());
			coreEvent.setTimestamp(verbTracer.getTimestamp());
			coreEvent.addPredicates(verbTracer.getPredicates());
			
			return (A)coreEvent;
			
		}else{
			LOGGER.debug("Cannot assign CoreEventLoad to {}", clazz.getName());
			return null;
		}
	}
	
	@Override 
	public void handleOperationData(){
		
		// trigger the audit event with audit load
		if(this.isAuditable()) {
			CoreEventLoad coreload = this.getOperationData(CoreEventLoad.class);
			EventDispatcher.getInstance().sendPayload(coreload);
		}
		
	}
	
	/**
	 * enter this method means no exception occurs, then end operation.
	 * close : end the operation automatically. 
	 **/
	@Override
	public void close(){
			
		endOperation(ExecState.SUCCESS, "operation success.");

	}
}
