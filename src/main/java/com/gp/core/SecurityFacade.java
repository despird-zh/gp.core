package com.gp.core;

import java.time.ZoneId;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.gp.svc.info.UserLiteInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gp.common.AccessPoint;
import com.gp.common.GeneralConstants;
import com.gp.common.IdKey;
import com.gp.common.IdKeys;
import com.gp.common.JwtPayload;
import com.gp.common.Operations;
import com.gp.common.GPrincipal;
import com.gp.common.ServiceContext;
import com.gp.common.SystemOptions;
import com.gp.common.GroupUsers;
import com.gp.common.GroupUsers.UserState;
import com.gp.exception.CoreException;
import com.gp.exception.ServiceException;
import com.gp.info.InfoId;
import com.gp.dao.info.SourceInfo;
import com.gp.dao.info.SysOptionInfo;
import com.gp.dao.info.TokenInfo;
import com.gp.info.KVPair;
import com.gp.dao.info.UserInfo;
import com.gp.pagination.PageQuery;
import com.gp.pagination.PageWrapper;
import com.gp.svc.CommonService;
import com.gp.svc.SourceService;
import com.gp.svc.SystemService;
import com.gp.svc.SecurityService;
import com.gp.svc.info.UserExtInfo;
import com.gp.util.ConfigSettingUtils;
import com.gp.util.HashUtils;
import com.gp.util.JwtTokenUtils;
import com.gp.validate.ValidateMessage;
import com.gp.validate.ValidateUtils;

/**
 * this class handle the security related operation, include group organization user etc.
 * 
 **/
@Component
public class SecurityFacade {
	
	static Logger LOGGER = LoggerFactory.getLogger(SecurityFacade.class);
	
	public static final String HASH_SALT = ConfigSettingUtils.getSystemOption(SystemOptions.SECURITY_HASH_SALT);
		
	private static SecurityService securityservice;

	private static SourceService masterservice;
	
	private static CommonService idservice;
	
	private static SystemService systemservice;
	
	@Autowired
	private SecurityFacade(SecurityService securityservice, 
			CommonService idservice,
			SourceService masterservice,
			SystemService systemservice){
		
		SecurityFacade.securityservice = securityservice;
		SecurityFacade.idservice = idservice;
		SecurityFacade.masterservice = masterservice;
		SecurityFacade.systemservice = systemservice;
	}
	
	public static UserInfo findAccountLite(AccessPoint accesspoint, 
			GPrincipal principal,
			InfoId<Long> userId,
			String account, String type) throws CoreException{
		
		UserInfo uinfo = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(principal, accesspoint)){
			
			svcctx.beginOperation(Operations.FIND_ACCOUNT.name(),  null, 
					new KVPair<String, String>("account",account));
			
			uinfo = securityservice.getAccountLite(svcctx, userId, account, type);
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.find.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		return uinfo;
	}
	
	/**
	 * Get the account information by account
	 * 
	 * @param ap the AccessPoint 
	 * @param account the account  
	 **/
	public static UserExtInfo findAccount(AccessPoint accesspoint, 
			GPrincipal principal,
			InfoId<Long> userId,
			String account, String type) throws CoreException{
		
		UserExtInfo uinfo = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(principal, accesspoint)){
			
			svcctx.beginOperation(Operations.FIND_ACCOUNT.name(),  null, 
					new KVPair<String, String>("account",account));
			
			uinfo = securityservice.getAccountFull(svcctx, userId, account, type);
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.find.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		return uinfo;
	}
	
	/**
	 * Create a new account in database
	 * 
	 * @param accesspoint the access point
	 * @param principal the principal object
	 * @param uinfo the user information
	 * @param pubcapacity public cabinet capacity
	 * @param pricapacity private cabinet capacity
	 **/
	public static boolean saveAccount(AccessPoint accesspoint,
			GPrincipal principal,
			UserInfo uinfo, Long pubcapacity, Long pricapacity)throws CoreException{

		Boolean result = false;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.UPDATE_ACCOUNT)){
			
			svcctx.addOperationPredicates(uinfo);
			// encrypt the password
			if(StringUtils.isNotBlank(uinfo.getPassword())){
				String hashpwd = HashUtils.hashEncodeBase64(uinfo.getPassword(), HASH_SALT);
				uinfo.setPassword(hashpwd);
			}
			// amend the operation information
			svcctx.setOperationObject(uinfo.getInfoId());

			result = securityservice.updateAccount(svcctx, uinfo, pubcapacity, pricapacity) > 0;
			
		} catch (ServiceException e) {
			ContextHelper.stampContext(e,"excp.save.account");
		}finally{
			ContextHelper.handleContext();
		}
		
		return result;
	}
	/**
	 * Create a new account in database
	 * 
	 * @param accesspoint the access point
	 * @param principal the principal object
	 * @param uinfo the user information
	 * @param pubcapacity public cabinet capacity
	 * @param pricapacity private cabinet capacity
	 **/
	public static InfoId<Long> newAccount(AccessPoint accesspoint,
			GPrincipal principal,
			UserInfo uinfo, Long pubcapacity, Long pricapacity) throws CoreException{
		
		InfoId<Long> result = null;
		
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.NEW_ACCOUNT)){
			svcctx.addOperationPredicates(uinfo);
			
			// set local entity id
			uinfo.setSourceId(GeneralConstants.LOCAL_SOURCE);
			// check the validation of user information
			Set<ValidateMessage> vmsg = ValidateUtils.validate(principal.getLocale(), uinfo);
			if(!CollectionUtils.isEmpty(vmsg)){ // fail pass validation
				CoreException coreexcp = new CoreException(svcctx.getPrincipal().getLocale(), "excp.validate");
				coreexcp.addValidateMessages(vmsg);
				throw coreexcp;
			}
			
			// encrypt the password
			String hashpwd = HashUtils.hashEncodeBase64(uinfo.getPassword(), HASH_SALT);
			uinfo.setPassword(hashpwd);
			// amend the information key data
			if(uinfo.getInfoId() == null){
				
				InfoId<Long> ukey = idservice.generateId( IdKey.GP_USERS, Long.class);
				uinfo.setInfoId(ukey);
			}
			
			// amend the operation information
			svcctx.setOperationObject(uinfo.getInfoId());

			securityservice.newAccount(svcctx, uinfo, pubcapacity, pricapacity);
			result = uinfo.getInfoId();
	
		} catch (Exception e) {
			ContextHelper.stampContext(e, "excp.save.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return result;
	}
	
	/**
	 * Create a new account in database
	 * 
	 * @param accesspoint the access point
	 * @param principal the principal object
	 * @param uinfo the user information
	 * @param pubcapacity public cabinet capacity
	 * @param pricapacity private cabinet capacity
	 **/
	public static InfoId<Long> newAccountExt(AccessPoint accesspoint,
			GPrincipal principal,
			UserInfo uinfo, String entity, String node) throws CoreException{
		
		InfoId<Long> result = null;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.NEW_ACCOUNT)){
			
			svcctx.addOperationPredicates(uinfo);
			// encrypt the password
			String hashpwd = HashUtils.hashEncodeBase64(uinfo.getPassword(), HASH_SALT);
			uinfo.setPassword(hashpwd);

			// check the validation of user information
			Set<ValidateMessage> vmsg = new HashSet<ValidateMessage>();
			SourceInfo instance = masterservice.getSource(svcctx, entity, node);
			if(instance != null){
				
				uinfo.setSourceId(instance.getInfoId().getId());
			}else{				
				vmsg.add(new ValidateMessage("source","target entity not existed"));
			}
			Set<ValidateMessage> vmsg1= ValidateUtils.validateProperty(svcctx.getPrincipal().getLocale(), "account", "globalAccount", "email", "mobile","fullName");
			vmsg.addAll(vmsg1);
			if(!CollectionUtils.isEmpty(vmsg)){ // fail pass validation
				CoreException coreexcp = new CoreException(svcctx.getPrincipal().getLocale(), "excp.validate");
				coreexcp.addValidateMessages(vmsg);
				throw coreexcp;
			}
			// amend the information key data
			if(!IdKeys.isValidId(uinfo.getInfoId())){
				
				InfoId<Long> ukey = idservice.generateId( IdKey.GP_USERS, Long.class);
				uinfo.setInfoId(ukey);
				
			}
			// amend the operation information
			svcctx.setOperationObject(uinfo.getInfoId());

			securityservice.newAccountExt(svcctx, uinfo);
			
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.save.account");
			
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return result;	
	}
	
	/**
	 * get accounts list from db by query condition
	 * 
	 * @param accesspoint the access point
	 * @param principal the principal object
	 * @param accountname the account name filter
	 * @param instance the instance filter, i.e. user original source
	 * 		  -9999 -> local ; null -> all ; 0 -> all external
	 * @param type the type filter
	 **/
	public static List<UserExtInfo> findAccounts(AccessPoint accesspoint,
			GPrincipal principal,
			String accountname, 
			Integer instanceId, 
			String[] types,
			String[] states)throws CoreException{
		
		List<UserExtInfo> result = null;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_ACCOUNTS)){
			
			String[][] parms = new String[][]{
				{"account",accountname},
				{"instance", instanceId == null ? "" : instanceId.toString()},
				{"types",ArrayUtils.toString(types)},
				{"state",ArrayUtils.toString(states)}};
				
			Map<?,?> parmap = ArrayUtils.toMap(parms);			
			svcctx.addOperationPredicates(parmap);

			// query accounts information
			result = securityservice.getAccounts(svcctx, accountname, instanceId, types,states);

		} catch (ServiceException e) {
			ContextHelper.stampContext(e, "excp.find.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		return result;
	}
	
	/**
	 * get accounts list from db by query condition
	 * 
	 * @param accesspoint the access point
	 * @param principal the principal object
	 * @param accountname the account name filter
	 * @param instance the instance filter, i.e. user original source
	 * @param type the type filter
	 **/
	public static PageWrapper<UserExtInfo> findAccounts(AccessPoint accesspoint,
			GPrincipal principal,
			String accountname, 
			Integer instanceId, 
			String[] type, 
			PageQuery pagequery)throws CoreException{
		
		PageWrapper<UserExtInfo> result = null;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_ACCOUNTS)){
			
			String[][] parms = new String[][]{
				{"account",accountname},
				{"instance", instanceId == null ? "" : instanceId.toString()},
				{"types",ArrayUtils.toString(type)}};
				
			Map<?,?> parmap = ArrayUtils.toMap(parms);			
			svcctx.addOperationPredicates(parmap);

			// query accounts information
			result = securityservice.getAccounts(svcctx, accountname, instanceId, type, pagequery);
		
		} catch (ServiceException e) {
			ContextHelper.stampContext(e,"excp.find.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		return result;
	}

	/**
	 * authenticate the password
	 **/
	public static Boolean authenticate(AccessPoint accesspoint,
			GPrincipal principal,
			String password)throws CoreException{
		boolean pass = false;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.AUTHENTICATE)){
			
			svcctx.addOperationPredicates(new DefaultKeyValue("password", password));
			byte[] pwdbytes;
			try {
				pwdbytes = Base64.getDecoder().decode(principal.getPassword());
				pass =  HashUtils.isExpectedPassword(password.toCharArray(), HASH_SALT.getBytes(), pwdbytes);
				
			} catch (Exception e) {
				LOGGER.error("password not match...", e);				
			}
			// password match means logon success reset the retry_times
			securityservice.updateLogonTrace(svcctx, principal.getUserId(), pass);
		} catch (ServiceException e) {
			ContextHelper.stampContext(e, "excp.authen");
		}finally{
			
			ContextHelper.handleContext();
		}
		return pass;
	}

	/**
	 * change the state of account
	 **/
	public static void changeAccountState(AccessPoint accesspoint,
			GPrincipal principal,
			UserState state)throws CoreException{
		
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(GroupUsers.PSEUDO_USER, accesspoint,
				Operations.CHANGE_ACCOUNT_STATE)){
			
			svcctx.addOperationPredicates(new DefaultKeyValue("state", state.name()));
			
			// password match means logon success reset the retry_times
			securityservice.changeAccountState(svcctx, principal.getUserId(), state);			
			
		} catch (ServiceException e) {
		
			ContextHelper.stampContext(e, "excp.change.state");
			
		}finally{
			
			ContextHelper.handleContext();
		}
	}

	/**
	 * remove the account from system
	 *
	 **/
	public static boolean removeAccount(AccessPoint accesspoint,
			GPrincipal principal,
			InfoId<Long> userId, String account)throws CoreException{
		
		Boolean gresult = false;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.REMOVE_ACCOUNT)){
			
			svcctx.addOperationPredicates(new KVPair<String,String>("account", account));
			
			// password match means logon success reset the retry_times
			gresult = securityservice.removeAccount(svcctx, userId, account);
			
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.remove_account");
			
		}finally{
			
			ContextHelper.handleContext();
		}
		return gresult;
	}

	/**
	 * change the account password
	 **/
	public static Boolean changePassword(AccessPoint accesspoint,
			GPrincipal principal,
			String account, 
			String password)throws CoreException{
		
		Boolean gresult = false;
		try (ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.CHANGE_PWD)){
			
			String hashpwd = HashUtils.hashEncodeBase64(password, HASH_SALT);
			// password match means logon success reset the retry_times
			gresult = securityservice.changePassword(svcctx, account, hashpwd);
			
		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.change.pwd");
			
		}finally{
			
			ContextHelper.handleContext();
		}
		return gresult;
	}

	/**
	 * Find the user lite information list
	 *
	 * @param userids target users id
	 * @param accounts target user accounts
	 *
	 **/
	public static List<UserLiteInfo> findAccountLites(AccessPoint accesspoint,
												  GPrincipal principal,
												  List<Long> userids,
												  List<String> accounts)throws CoreException{

		List<UserLiteInfo> result = null;

		try(ServiceContext svcctx = ContextHelper.beginServiceContext(principal, accesspoint,
				Operations.FIND_ACCOUNTS)){

			result = securityservice.getAccounts(svcctx, userids, accounts);

		} catch (Exception e) {

			ContextHelper.stampContext(e,"excp.find.account");
		}finally{

			ContextHelper.handleContext();
		}
		return result;
	}
	
	/**
	 * Find a principal from database 
	 **/
	public static GPrincipal findPrincipal(AccessPoint accesspoint,
			InfoId<Long> userId,
			String account, String type) throws CoreException{
		
		GPrincipal principal = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(GroupUsers.PSEUDO_USER, accesspoint)){
			UserInfo uinfo = null;
			
			svcctx.beginOperation(Operations.FIND_ACCOUNT.name(),  null, 
					new KVPair<String, String>("account",account));
			
			uinfo = securityservice.getAccountLite(svcctx, userId, account, type);
			if(null == uinfo)
				throw new CoreException("excp.find.account");
			
			principal = new GPrincipal(uinfo.getInfoId());
			principal.setSourceId(uinfo.getSourceId());
			principal.setAccount(account);
	        principal.setEmail(uinfo.getEmail());
	        principal.setPassword(uinfo.getPassword());
	        
	        // zh_CN / en_US / fr_FR
	        String[] localeStr = StringUtils.split(uinfo.getLanguage(), "_");
	        Locale locale = Locale.ENGLISH;
	        if(localeStr.length == 2){
	        	locale = new Locale(localeStr[0], localeStr[1]);
	        }
	        principal.setLocale(locale);
	         
	 		principal.setTimeZone(ZoneId.of(uinfo.getTimezone()));

		} catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.find.account");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return principal;
		
	}
	
	/**
	 * find a token by token id 
	 **/
	public static TokenInfo findToken(AccessPoint accesspoint,
			InfoId<Long> tokenId) throws CoreException{
		
		TokenInfo token = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(GroupUsers.PSEUDO_USER, accesspoint)){
			
			svcctx.beginOperation(Operations.FIND_TOKEN.name(),  null,
					new KVPair<String, String>("token_id", String.valueOf(tokenId.getId())));
			
			token = securityservice.getToken(svcctx, tokenId);
			
		}catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.find.token");
		}finally{
			
			ContextHelper.handleContext();
		}
		return token;
	}
	
	/**
	 * create a new token by JWT payload, there will be a token per subject & audience.
	 * 
	 * @param payload the JWT payload
	 * @return String the JWT token string 
	 **/
	public static String newToken(AccessPoint accesspoint, JwtPayload payload) 
			throws CoreException{
		
		String token = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(GroupUsers.PSEUDO_USER, accesspoint)){
			
			svcctx.beginOperation(Operations.NEW_TOKEN.name(),  null,
					payload);
			
			SysOptionInfo option = systemservice.getOption(svcctx, SystemOptions.SECURITY_JWT_SECRET);

			InfoId<Long> tokenId = idservice.generateId(IdKey.GP_TOKENS, Long.class);
			TokenInfo tokenInfo = new TokenInfo();
			
			tokenInfo.setInfoId(tokenId);
			payload.setJwtId(String.valueOf(tokenId.getId()));
			
			tokenInfo.setAudience(payload.getAudience());
			tokenInfo.setIssuer(payload.getIssuer());
			tokenInfo.setSubject(payload.getSubject());
			tokenInfo.setIssueAt(payload.getIssueAt());
			tokenInfo.setExpireTime(payload.getExpireTime());
			tokenInfo.setNotBefore(payload.getNotBefore());
			
			svcctx.setTraceInfo(tokenInfo);
			
			token = JwtTokenUtils.signHS256(option.getOptionValue(), payload);
			tokenInfo.setJwtToken(token);
			
			securityservice.newToken(svcctx, tokenInfo);
			
		}catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.find.token");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return token;
	}
	
	/**
	 * Reissue a new token by JWT payload, there will be a token per subject & audience.
	 * 
	 * @param payload the JWT payload
	 * @return String the JWT token string 
	 **/
	public static String reissueToken(AccessPoint accesspoint,GPrincipal principal, JwtPayload payload) 
			throws CoreException{
		
		String token = null;
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(principal, accesspoint)){
			
			svcctx.beginOperation(Operations.REISSUE_TOKEN.name(),  null,
					payload);
			
			SysOptionInfo option = systemservice.getOption(svcctx, SystemOptions.SECURITY_JWT_SECRET);
			Long jwtid = NumberUtils.toLong(payload.getJwtId());
			if(jwtid < 0){
				throw new CoreException("excp.reissue.token");
			}
			InfoId<Long> tokenId = IdKeys.getInfoId(IdKey.GP_TOKENS,jwtid);
			
			TokenInfo tokenInfo = new TokenInfo();
			
			tokenInfo.setInfoId(tokenId);

			tokenInfo.setIssueAt(payload.getIssueAt());
			tokenInfo.setExpireTime(payload.getExpireTime());
			tokenInfo.setNotBefore(payload.getNotBefore());
			
			svcctx.setTraceInfo(tokenInfo);
			
			token = JwtTokenUtils.signHS256(option.getOptionValue(), payload);
			tokenInfo.setJwtToken(token);
			
			securityservice.refreshToken(svcctx, tokenInfo);
			
		}catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.reissue.token");
		}finally{
			
			ContextHelper.handleContext();
		}
		
		return token;
	}
	
	/**
	 * Log off system meant to remove the token
	 * 
	 * @param tokenId the id of token
	 **/
	public static boolean removeToken(AccessPoint accesspoint,GPrincipal principal, InfoId<Long> tokenId) 
			throws CoreException{
		
		try (ServiceContext svcctx = ContextHelper.buildServiceContext(principal, accesspoint)){
			
			svcctx.beginOperation(Operations.LOGOFF.name(), tokenId,null);
			
			return securityservice.removeToken(svcctx, tokenId);
			
		}catch (ServiceException e) {
			
			ContextHelper.stampContext(e, "excp.reissue.token");
		}finally{
			
			ContextHelper.handleContext();
		}
		return true;
	}
}
