package com.gp.core;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.common.GeneralContext.ExecState;
import com.gp.common.AccessPoint;
import com.gp.common.Operations;
import com.gp.common.GPrincipal;
import com.gp.common.ServiceContext;
import com.gp.common.SystemOptions;
import com.gp.exception.BaseException;
import com.gp.exception.CoreException;
import com.gp.web.util.ConfigUtils;

/**
 * Provides a way to build context object, possible implements SPI in future
 * As for the ServiceContext post persistence operation, do audit stuff here.
 * 
 * @author gary diao
 * @version 0.1 2014-12-12
 * 
 **/
public class ContextHelper {
	
	public static Logger LOGGER = LoggerFactory.getLogger(ContextHelper.class);
	
	/**
	 * Use thread local to hold current thread service context
	 * 
	 **/
	private static final ThreadLocal<ServiceContext> threadLocal = new ThreadLocal<ServiceContext>(); 
    
	/**
	 * keep the service context in thread local
	 **/
    private static final void keepContext(ServiceContext svcctx){
        threadLocal.set(svcctx);
    }
    
    /**
     * drop the service context out of thread local
     **/
    private static final void dropContext(){
        threadLocal.set(null);
    }
	
    /**
     * get the service context from thread local
     **/
    public static final ServiceContext getContext(){
    	
    	return threadLocal.get();
    }
    
	/**
	 * build service context object depends on the configuration setting.
	 * 
	 * @param principal the principal object
	 * @param accesspoint the access point object
	 * 
	 **/
	public static ServiceContext buildServiceContext(GPrincipal principal, AccessPoint accesspoint){
		
		String audit = ConfigUtils.getSystemOption(SystemOptions.AUDIT_ENABLE);
		//GeneralConfig.getString(GeneralConstants.ENV_AUDIT_ENABLE);
		
		audit = StringUtils.isBlank(audit) ? "true" : audit;
		Boolean auditenable = Boolean.valueOf(audit);
		
		ServiceContext svcctx = null;
		if(auditenable){
			
			svcctx = new CoreServiceContext(principal, accesspoint);

		}else{
			
			svcctx = new ServiceContext(principal);
		}
		// keep context to thread local
		keepContext(svcctx);
		
		return svcctx;
	}
	
	/**
	 * build service context object depends on the configuration setting. 
	 * and begin the specified operation and primary verb.
	 * 
	 * @param principal the principal object
	 * @param accesspoint the access point object
	 * @param operation the operation name
	 * @param verb the verb name
	 **/
	public static ServiceContext beginServiceContext(GPrincipal principal, AccessPoint accesspoint, String verb){
		
		ServiceContext svcctx = buildServiceContext(principal, accesspoint);
		// if auditable then begin operation with blind object and predicates
		svcctx.beginOperation(verb, null, null);
		
		return svcctx;
	}
	
	/**
	 * build service context object depends on the configuration setting. 
	 * and begin the specified operation and primary verb.
	 * 
	 * @param principal the principal object
	 * @param accesspoint the access point object
	 * @param operation the operation name
	 * @param verb the verb name
	 **/
	public static ServiceContext beginServiceContext(GPrincipal principal, AccessPoint accesspoint, Operations operation){
		
		ServiceContext svcctx = buildServiceContext(principal, accesspoint);
		// if auditable then begin operation with blind object and predicates
		svcctx.beginOperation(operation.name(), null, null);
		
		return svcctx;
	}
	
	/**
	 * If only principal available, provides ServiceContext only.
	 * 
	 * @param principal the principal object
	 * 
	 **/
	public static ServiceContext buildServiceContext(GPrincipal principal){
				
		ServiceContext svcctx = new ServiceContext(principal);
		
		keepContext(svcctx);
		return svcctx;
	}
		
	/**
	 * Extract audit data out of AuditServiceContext and transfer it to event engine<br>
	 * !!! Important !!!<br>
	 * this method fetch service context from thread local, and drop it post persistence.
	 **/
	public static void handleContext(){
		
		ServiceContext svcctx = getContext();
		// clear the thread local context object.
		dropContext();
		// if valid ServiceContext continue work.
		if(null != svcctx){
			svcctx.endOperation(ExecState.SUCCESS, "Process success");
			svcctx.handleOperationData();
		}
	}
	
	public static void stampContext(Exception e) throws CoreException{
		
		stampContext(e, "excp.unknown");
	}
	
	/**
	 * Stamp the exception on service context.
	 * 
	 * @param e the exception 
	 * @param excpcode the code to fetch locale message
	 **/
	public static void stampContext(Exception e, String msgcode) throws CoreException{
		
		ServiceContext svcctx = getContext();
		
		// if valid ServiceContext continue work.
		if(null != svcctx){
			Throwable[] suppressed = e.getSuppressed();
			// fail persist audit data in ServiceContext.close();
			if(suppressed != null && suppressed.length > 0){
				
				// means error occurs in both close() and try-with closure.
				svcctx.endOperation(ExecState.EXCEP, suppressed[0].getMessage());
			}else{
				
				svcctx.endOperation(ExecState.EXCEP, e.getMessage());
			}
		}
		
		// message processing
		String excpcode = msgcode;
		if(e instanceof BaseException && !((BaseException)e).matched()){
			// matched the getCode return the message, otherwise return error code directly
			excpcode = ((BaseException)e).getCode();
		}
		
		if(e instanceof CoreException){
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Audit collector stamp on the exception", e);
			throw (CoreException)e;
		}else{
			LOGGER.error("Audit collector stamp on the exception", e);
			throw new CoreException(svcctx.getPrincipal().getLocale(), excpcode, e);
		}
	}
}
