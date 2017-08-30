package com.gp.web.api;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gp.common.AccessPoint;
import com.gp.common.IdKey;
import com.gp.common.IdKeys;
import com.gp.common.JwtPayload;
import com.gp.common.GPrincipal;
import com.gp.core.SecurityFacade;
import com.gp.exception.CoreException;
import com.gp.info.InfoId;
import com.gp.util.DateTimeUtils;
import com.gp.util.JwtTokenUtils;
import com.gp.web.ActionResult;
import com.gp.web.BaseController;
import com.gp.web.servlet.ServiceTokenFilter;
import com.gp.web.servlet.ServiceTokenFilter.AuthTokenState;

@Controller
@RequestMapping(ServiceTokenFilter.FILTER_PREFIX)
public class AuthenController extends BaseController{

	static Logger LOGGER = LoggerFactory.getLogger(AuthenController.class);
	
	@RequestMapping(
		    value = "authenticate", 
		    method = RequestMethod.POST,
		    consumes = {"text/plain", "application/*"})
	public ModelAndView doAuthenticate(@RequestBody String payload) throws Exception {
		
		AccessPoint accesspoint = super.getAccessPoint(request);
		// the model and view
		ModelAndView mav = super.getJsonModelView();
		
		Map<String, String> map = JACKSON_MAPPER.readValue(payload, new TypeReference<Map<String, String>>(){});
		String account = map.get("principal");
		String password = map.get("credential");
		String audience = map.get("audience");
		
		ActionResult result = authenAccount(accesspoint, audience, account, password);
		return mav.addAllObjects(result.asMap());
	}
	
	@RequestMapping(
			value = "authenticate",
			method = RequestMethod.GET)
	public ModelAndView doAuthenticate(){	
		// the access point
		AccessPoint accesspoint = super.getAccessPoint(request);
		// the model and view
		ModelAndView mav = super.getJsonModelView();
		
		String account = readRequestParam("principal");
		String password = readRequestParam("credential");
		String audience = readRequestParam("audience");
		
		ActionResult result = authenAccount(accesspoint, audience, account, password);
		
		return mav.addAllObjects(result.asMap());
	}
	
	@RequestMapping(
		    value = "reissue", 
		    consumes = {"text/plain", "application/*"})
	public ModelAndView doReissue() {
		
		AccessPoint accesspoint = super.getAccessPoint(request);
		GPrincipal principal = super.getPrincipal();
		// the model and view
		ModelAndView mav = super.getJsonModelView();
		ActionResult result = null;
		String token = request.getHeader(ServiceTokenFilter.AUTH_HEADER);
		token = StringUtils.substringAfter(token, "Bearer: ");
		JwtPayload jwtPayload = JwtTokenUtils.parsePayload(token);
	
		jwtPayload.setNotBefore(DateTimeUtils.now());
		jwtPayload.setIssueAt(DateTimeUtils.now());
		jwtPayload.setExpireTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000 ));
		
		try{
			String mesg = super.getMessage("mesg.reissue.token");
			String newtoken = SecurityFacade.reissueToken(accesspoint, principal, jwtPayload);
			result = ActionResult.success(mesg);
			result.setData(newtoken);
			result.getMeta().setCode(AuthTokenState.REISSUE_TOKEN.name());
			
		}catch(CoreException ce){
			result = ActionResult.error(ce.getLocalizedMessage());
			result.getMeta().setCode(AuthTokenState.FAIL_AUTHC.name());
		}
		
		return mav.addAllObjects(result.asMap());
	}
	
	@RequestMapping(
		    value = "logoff", 
		    consumes = {"text/plain", "application/*"},
		    method = RequestMethod.GET)
	public ModelAndView doLogoff() {
		
		AccessPoint accesspoint = super.getAccessPoint(request);
		// the model and view
		ModelAndView mav = super.getJsonModelView();
		GPrincipal principal = super.getPrincipal();
		ActionResult result = null;
		
		String token = request.getHeader(ServiceTokenFilter.AUTH_HEADER);
		token = StringUtils.substringAfter(token, "Bearer: ");
		JwtPayload jwtPayload = JwtTokenUtils.parsePayload(token);
		
		try{
			Long jwtid = NumberUtils.toLong(jwtPayload.getJwtId());
			InfoId<Long> tokenId = IdKeys.getInfoId(IdKey.TOKEN,jwtid);
			String mesg = super.getMessage("mesg.remove.token");
			boolean done = SecurityFacade.removeToken(accesspoint, principal, tokenId);
			
			result = done? ActionResult.success(mesg) : ActionResult.failure(getMessage("excp.remove.token"));

		}catch(CoreException ce){
			result = ActionResult.error(ce.getLocalizedMessage());
		}
		return mav.addAllObjects(result.asMap());
	}
	/**
	 * Verify the password and return the Result 
	 **/
	private ActionResult authenAccount(AccessPoint accesspoint, String audience, String account, String password){
		ActionResult result = null;
		try{
			if(StringUtils.isBlank(audience) ||
					StringUtils.isBlank(account) ||
					StringUtils.isBlank(password) ){
				
				String mesg = super.getMessage("excp.param.miss");
				result = ActionResult.failure(mesg);
			}
			
			GPrincipal principal = SecurityFacade.findPrincipal(accesspoint, null, account, null);
			if(null == principal){
				String mesg = super.getMessage("excp.no.principal");
				result = ActionResult.failure(mesg);
				
			}else{
				// authenticate the subject & credential
				boolean pass = SecurityFacade.authenticate(accesspoint, principal, password);
				
				if(pass){
					String mesg = super.getMessage("mesg.pwd.pass");
					
					JwtPayload payload = new JwtPayload();
					payload.setIssuer("gp.svc.svr");
					payload.setSubject(account);
					if(StringUtils.isNotBlank(audience))
						payload.setAudience(audience);
					
					payload.setNotBefore(DateTimeUtils.now());
					payload.setIssueAt(DateTimeUtils.now());
					payload.setExpireTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000 ));
					
					String token = SecurityFacade.newToken(accesspoint, payload);
					result = ActionResult.success(mesg);
					result.getMeta().setCode(AuthTokenState.VALID_TOKEN.name());
					result.setData(token);
					
				}else{
					String mesg = super.getMessage("excp.pwd.wrong");
					result = ActionResult.failure(mesg);
					result.getMeta().setCode(AuthTokenState.FAIL_AUTHC.name());
				}
			}
			
		}catch(CoreException ce){
			result = ActionResult.error(ce.getLocalizedMessage());
			result.getMeta().setCode(AuthTokenState.FAIL_AUTHC.name());
		}

		return result;
	}
}
