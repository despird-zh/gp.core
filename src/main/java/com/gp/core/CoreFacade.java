package com.gp.core;

import com.gp.dao.info.OperLogInfo;
import com.gp.svc.OperLogService;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gp.common.IdKey;
import com.gp.common.ServiceContext;
import com.gp.exception.CoreException;
import com.gp.exception.ServiceException;
import com.gp.dao.info.AuditInfo;
import com.gp.info.InfoId;
import com.gp.svc.AuditService;
import com.gp.svc.CommonService;

/**
 * CoreFacade provides methods to handle the core event data
 * <ol>
 *     <li>process the audit event data</li>
 *     <li>process the action log data</li>
 *     <li>process the core event and generate the notification</li>
 * </ol>
 *
 * @author  gary diao
 * @version  0.1 2015-10-10
 *
 **/
@Component
public class CoreFacade {

	static Logger LOGGER = LoggerFactory.getLogger(CoreFacade.class);
	
	private static AuditService auditservice;

	private static CommonService idService;

	private static OperLogService operlogservice;

	@Autowired
	private CoreFacade(AuditService auditservice,
					   OperLogService operlogservice,
					   CommonService idService){
		
		CoreFacade.auditservice = auditservice;
		CoreFacade.idService = idService;
		CoreFacade.operlogservice = operlogservice;
	}
	
	/**
	 * Audit the verbs generated from operation, here not user AuditServiceConext as for no need to collect audit information.
	 * otherwise it falls into dead loop.
	 * 
	 * @param auditlist the audit data of operation
	 * 
	 **/
	public static void auditOperation(AuditInfo auditinfo) throws CoreException{
		
		if(null == auditinfo)
			return;
		
		try {
			ServiceContext svcctx = ServiceContext.getPseudoServiceContext();
			// retrieve and set audit id
			InfoId<Long> auditId = idService.generateId( IdKey.AUDIT, Long.class);
			auditinfo.setInfoId(auditId); 

			auditservice.addAudit(svcctx, auditinfo);
		
		} catch (ServiceException e) {
			throw new CoreException("fail to get admin principal",e );
		}
	}

	/**
	 * Handle the core event payload
	 * @param  coreload the payload of event
	 **/
	public static void handleUpdateAccount(CoreEventLoad coreload)throws CoreException{

		try {
			ServiceContext svcctx = ServiceContext.getPseudoServiceContext();
			OperLogInfo operinfo = new OperLogInfo();
			InfoId<Long> operid = idService.generateId(IdKey.OPER_LOG, Long.class);
			operinfo.setInfoId(operid);
			operinfo.setAccount(coreload.getOperator());
			operinfo.setOperation(coreload.getOperation());
			
			if(InfoId.isValid(coreload.getObjectId()))
				operinfo.setObjectId(coreload.getObjectId().toString());
			
			operinfo.setOperationTime(new Date(System.currentTimeMillis()));
			
			svcctx.setTraceInfo(operinfo);
			operlogservice.addOperLog(svcctx, operinfo);

		}catch (ServiceException e) {
			throw new CoreException("fail to handle the core event payload",e );
		}
	}
}
