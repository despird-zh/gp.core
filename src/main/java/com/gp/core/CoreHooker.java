package com.gp.core;

import com.gp.audit.AuditConverter;
import com.gp.common.AccessPoint;
import com.gp.common.Operations;
import com.gp.dao.info.AuditInfo;
import com.gp.exception.CoreException;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.disruptor.EventHooker;
import com.gp.disruptor.EventPayload;
import com.gp.disruptor.EventType;
import com.gp.exception.RingEventException;

/**
 * This hooker class digest all the core event which loaded with data.
 * the data include measure etc.
 * 
 * @author gary diao
 * @version 0.1 2015-12-8
 **/
public class CoreHooker extends EventHooker<CoreEventLoad>{

	static Logger LOGGER = LoggerFactory.getLogger(CoreHooker.class);
	
	public CoreHooker() {
		super(EventType.CORE);
	}

	@Override
	public void processPayload(EventPayload payload) throws RingEventException {

		if(!(payload instanceof CoreEventLoad)){
			return;
		}

		CoreEventLoad coreload = (CoreEventLoad) payload;
		persistLocal(coreload);

	}


	/**
	 * Persist the audit data locally to database directly. 
	 **/
	private void persistLocal(CoreEventLoad payload) throws RingEventException {

		// prepare access point
		AccessPoint apt = payload.getAccessPoint();

		// prepare the operation primary audit
		AuditInfo operaudit = new AuditInfo();	
		operaudit.setSubject(payload.getOperator());
		
		if(null != payload.getObjectId())
			operaudit.setTarget(payload.getObjectId().toString());
		
		operaudit.setOperation(payload.getOperation());
		MutableObject pjson;
		try {
			pjson = AuditConverter.mapToJson(payload.getPredicates());
			operaudit.setPredicates((String)pjson.getValue());
		} catch (IOException e) {
			LOGGER.error("error to convert predicate map");
		}
		
		operaudit.setApp(apt.getApp());
		operaudit.setClient(apt.getClient());
		operaudit.setHost(apt.getHost());
		operaudit.setVersion(apt.getVersion());
		
		if(payload.getWorkgroupId() != null)
			operaudit.setWorkgroupId(payload.getWorkgroupId().getId());
		
		operaudit.setState(payload.getState());
		operaudit.setMessage(payload.getMessage());
		operaudit.setAuditDate(new Date(payload.getTimestamp()));
		operaudit.setElapseTime(payload.getElapsedTime());
		
		try {
			// store data to database.
			CoreFacade.auditOperation(operaudit);
		} catch (CoreException e) {
			
			LOGGER.error("Fail to persist audit to database.",e);
			throw new RingEventException("Fail to persist audit to database.",e);
		}
	}
}
