package com.gp.core;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.audit.AuditConverter;
import com.gp.common.AccessPoint;
import com.gp.dao.info.AuditInfo;
import com.gp.disruptor.EventPayload;
import com.gp.disruptor.EventType;
import com.gp.disruptor.RingEvent;
import com.gp.exception.CoreException;
import com.gp.exception.RingEventException;
import com.gp.info.InfoId;
import com.lmax.disruptor.EventHandler;

/**
 * The core audit handler cover the audit persistence operation 
 * @author gdiao
 * @version 0.1 2016-10-21
 * 
 **/
public class CoreAuditHandler implements EventHandler<RingEvent>{
	
	static Logger LOGGER = LoggerFactory.getLogger(CoreAuditHandler.class);
	
	@Override
	public void onEvent(RingEvent event, long sequence, boolean endOfBatch) throws Exception {
		
		if(event.getEventType() != EventType.CORE) return;
		
		EventPayload payload = event.getPayload();
		
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
		try {
			operaudit.setPredicates(AuditConverter.mapToJson(payload.getPredicates()));
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
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Operation : {} / User : {} / Elapse : {}", payload.getOperation(), payload.getOperator(), payload.getElapsedTime());
		}
		try {
			// store data to database.
			InfoId<Long> auditId = CoreFacade.auditOperation(operaudit);
			payload.setAutidId(auditId);
		} catch (CoreException e) {
			
			LOGGER.error("Fail to persist audit to database.",e);
			throw new RingEventException("Fail to persist audit to database.",e);
		}
	}
}
