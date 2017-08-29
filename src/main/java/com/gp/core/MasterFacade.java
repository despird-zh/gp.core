package com.gp.core;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gp.common.AccessPoint;
import com.gp.common.Operations;
import com.gp.common.GPrincipal;
import com.gp.common.ServiceContext;
import com.gp.exception.CoreException;
import com.gp.exception.ServiceException;
import com.gp.dao.info.SysOptionInfo;
import com.gp.svc.SystemService;

@Component
public class MasterFacade {

	static Logger LOGGER = LoggerFactory.getLogger(MasterFacade.class);
	
	private static SystemService systemservice;

	@Autowired
	private MasterFacade(SystemService systemservice){
		MasterFacade.systemservice = systemservice;
	}
	
	/**
	 * Save the system option setting 
	 **/
	public static Boolean saveSystemOption(AccessPoint accesspoint,
			GPrincipal principal,
			String optionKey,
			String optionValue)throws CoreException{
		
		Boolean result = false;
		
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.UPDATE_SYSOPTION)){
			
			SysOptionInfo sinfo = systemservice.getOption(svcctx, optionKey);
			svcctx.setOperationObject(sinfo.getInfoId());
			svcctx.addOperationPredicates(new DefaultKeyValue("value", optionValue));

			result =  systemservice.updateOption(svcctx, optionKey, optionValue);
		} catch (ServiceException e)  {
			ContextHelper.stampContext(e, "excp.save.sysopt");
		}finally{
			ContextHelper.handleContext();
		}
		return result;		
		
	}
	
	/**
	 * find the system options by group key 
	 **/
	public static List<SysOptionInfo> findSystemOptions(AccessPoint accesspoint,
			GPrincipal principal,
			String groupKey)throws CoreException{
			
		List<SysOptionInfo> result = null;		
		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_SYSOPTIONS)){
			
			String[][] parms = new String[][]{
				{"groupkey",groupKey}};				
			Map<?,?> parmap = ArrayUtils.toMap(parms);			
			svcctx.addOperationPredicates(parmap);

			// query accounts information
			result = systemservice.getOptions(svcctx, groupKey);

		} catch (Exception e) {
		
			ContextHelper.stampContext(e, "excp.find.sysopts");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return result;
	}
	
	/**
	 * find the system options by group key 
	 **/
	public static SysOptionInfo findSystemOption(AccessPoint accesspoint,
			GPrincipal principal,
			String optionKey)throws CoreException{
			
		SysOptionInfo result = null;		
		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_SYSOPTIONS)){
			
			String[][] parms = new String[][]{
				{"optkey",optionKey}};	
				
			Map<?,?> parmap = ArrayUtils.toMap(parms);			
			svcctx.addOperationPredicates(parmap);

			// query accounts information
			result = systemservice.getOption(svcctx, optionKey);

		} catch (Exception e) {
		
			ContextHelper.stampContext(e, "excp.find.sysopts");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return result;
	}
	
	/**
	 * find the system option groups 
	 **/
	public static List<String> findSystemOptionGroups(AccessPoint accesspoint,
			GPrincipal principal)throws CoreException{
		
		List<String> result = null;
		
		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_SYSOPT_GRPS)){

			result = systemservice.getOptionGroups(svcctx);

		} catch (Exception e) {
			
			ContextHelper.stampContext(e,"excp.find.sysopt.group");
		}finally{
			
			ContextHelper.handleContext();
		}
		return result;
	}

}
