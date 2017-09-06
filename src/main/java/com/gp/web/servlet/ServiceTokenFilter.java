package com.gp.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gp.common.AccessPoint;
import com.gp.common.GroupUsers;
import com.gp.common.IdKey;
import com.gp.common.IdKeys;
import com.gp.common.JwtPayload;
import com.gp.common.GPrincipal;
import com.gp.common.SystemOptions;
import com.gp.core.MasterFacade;
import com.gp.core.SecurityFacade;
import com.gp.dao.info.SysOptionInfo;
import com.gp.dao.info.TokenInfo;
import com.gp.exception.CoreException;
import com.gp.info.InfoId;
import com.gp.util.JwtTokenUtils;
import com.gp.web.util.ExWebUtils;

/**
 * This filter customize as per the CorsFilter, add the authentication process 
 * 
 * @author diaogc
 * @version 0.1 2016-10-20 
 **/
public class ServiceTokenFilter extends OncePerRequestFilter {

	static Logger LOGGER = LoggerFactory.getLogger(ServiceTokenFilter.class);
	
	public static final String TOKEN_PREFIX = "Bearer: ";
	public static final String AUTH_HEADER = "Authorization";
	
	public static final String BLIND_TOKEN = "Bearer: __blind_token__";
	
	public static final String FILTER_STATE = "_svc_filter_state";
	
	public static final String ACT_AUTH_TOKEN = "/authenticate";
	public static final String ACT_BAD_TOKEN = "/bad-token";
	public static final String ACT_TRAP_ALL = "/trap";
	public static final String ACT_REISSUE_TOKEN = "/reissue";
	
	/**
	 * the patter will be /p1/p2, remember it will be applied to controller annotation.
	 * so don't change it pattern 
	 **/
	public static final String FILTER_PREFIX = "/gpapi";
	
	private UrlMatcher urlMatcher = null;
	
	/**
	 * Define the state of request 
	 * 
	 **/
	public static enum AuthTokenState{
		
		NEED_AUTHC, // need authenticate
		FAIL_AUTHC, // fail authenticate
		BAD_TOKEN,  // bad token 
		GHOST_TOKEN, // ghost token, token not exist in db
		INVALID_TOKEN, // invalid token
		VALID_TOKEN, // valid token 
		REISSUE_TOKEN,
		EXPIRE_TOKEN,//
		UNKNOWN;
	}
	
	private final CorsConfigurationSource configSource;

	private CorsProcessor processor = new DefaultCorsProcessor();


	/**
	 * Constructor accepting a {@link CorsConfigurationSource} used by the filter
	 * to find the {@link CorsConfiguration} to use for each incoming request.
	 * @see UrlBasedCorsConfigurationSource
	 */
	public ServiceTokenFilter(CorsConfigurationSource configSource) {
		Assert.notNull(configSource, "CorsConfigurationSource must not be null");
		this.configSource = configSource;
	}
	
	/**
	 * Configure a custom {@link CorsProcessor} to use to apply the matched
	 * {@link CorsConfiguration} for a request.
	 * <p>By default {@link DefaultCorsProcessor} is used.
	 */
	public void setCorsProcessor(CorsProcessor processor) {
		Assert.notNull(processor, "CorsProcessor must not be null");
		this.processor = processor;
	}

	public void setUrlMatcher(UrlMatcher urlMatcher) {
		this.urlMatcher = urlMatcher;
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		if(null == this.urlMatcher)
			return super.shouldNotFilter(request);
		else 
			return !this.urlMatcher.match(request);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		if (CorsUtils.isCorsRequest(request)) {
			CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(request);
			if (corsConfiguration != null) {
				boolean isValid = this.processor.processRequest(corsConfiguration, request, response);
				if (!isValid || CorsUtils.isPreFlightRequest(request)) {
					return;
				}
			}
		}
		
		/**
		 * Set the buffer size of response 
		 **/
		HttpServletResponse origin = ExWebUtils.getNativeResponse(response, HttpServletResponse.class);
		origin.setBufferSize(402800);
		/**
		 * filterChain.doFilter(request, response);
		 * append further authentication here.
		 **/
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		if(LOGGER.isDebugEnabled()){
			ExWebUtils.dumpRequestAttributes(httpRequest);
			LOGGER.debug("Filter URL:{}", request.getRequestURI());
		}
		String token = httpRequest.getHeader(AUTH_HEADER);
		AuthTokenState state = AuthTokenState.UNKNOWN;

		AccessPoint accesspoint = ExWebUtils.getAccessPoint(httpRequest);
		if(StringUtils.isBlank(token) || StringUtils.equalsIgnoreCase(BLIND_TOKEN, token)){

			// don't have token, forward request to authenticate it
			state = AuthTokenState.NEED_AUTHC;

		}else{
			if(StringUtils.startsWith(token, TOKEN_PREFIX)) {
				token = StringUtils.substringAfter(token, TOKEN_PREFIX);
			}
			JwtPayload jwtPayload = JwtTokenUtils.parsePayload(token);
			
			if(null == jwtPayload){
				// can not parse the payload
				state = AuthTokenState.BAD_TOKEN;
				
			}else{

				try{
					InfoId<Long> tokenId = IdKeys.getInfoId(IdKey.GP_TOKENS, NumberUtils.toLong(jwtPayload.getJwtId()));
					TokenInfo tokenInfo = SecurityFacade.findToken(accesspoint, tokenId);
					// check if the token record exists
					if(tokenInfo == null){
						// not find any token in db
						state = AuthTokenState.GHOST_TOKEN;
					}else{
						SysOptionInfo secret = MasterFacade.findSystemOption(accesspoint, GroupUsers.PSEUDO_USER, SystemOptions.SECURITY_JWT_SECRET);
						
						if(!StringUtils.equals(tokenInfo.getJwtToken(), token)){
							
							state = AuthTokenState.INVALID_TOKEN;
						}
						else{
							int valid =  JwtTokenUtils.verifyHS256(secret.getOptionValue(), token, jwtPayload);
							if(valid < 0){
								
								state = AuthTokenState.INVALID_TOKEN;
							}
							else if(valid == JwtTokenUtils.EXPIRED){
								
								state = AuthTokenState.EXPIRE_TOKEN;
							}
							else{
								
								state = AuthTokenState.VALID_TOKEN;
								// attach the state to request
								request.setAttribute(FILTER_STATE, state);
								// attach principal to request
								GPrincipal principal = SecurityFacade.findPrincipal(accesspoint, null, jwtPayload.getSubject(), null);
								ExWebUtils.setPrincipal(httpRequest, principal);
								// a valid token, continue the further process
								filterChain.doFilter(request, response);
								return;
							}
						}
					}
				}catch(CoreException ce){
					state = AuthTokenState.BAD_TOKEN;
					LOGGER.error("Fail to get the jwt token record",ce);
				}
			}
		}
		if(request.getRequestURI().startsWith(FILTER_PREFIX + ACT_REISSUE_TOKEN)){
			// valid token for refresh, forward it directly.
			state = AuthTokenState.REISSUE_TOKEN;
		}
		// trap all the invalid token request
		request.setAttribute(FILTER_STATE, state);
		forward(request, response, state);
	}

	/**
	 * forward the request as per the state of request and token
	 * @param request
	 * @param response
	 * @param state 
	 * 
	 **/
	private void forward(ServletRequest request, ServletResponse response, AuthTokenState state){
		RequestDispatcher dispatcher = null;
		
		FilterConfig filterConfig = this.getFilterConfig();
		
		try {
			if(AuthTokenState.NEED_AUTHC == state){
			
				dispatcher = filterConfig.getServletContext().getRequestDispatcher(FILTER_PREFIX + ACT_AUTH_TOKEN);
			
			}else if(AuthTokenState.BAD_TOKEN == state ||
					AuthTokenState.GHOST_TOKEN == state ||
					AuthTokenState.INVALID_TOKEN == state ||
					AuthTokenState.EXPIRE_TOKEN == state){
			
				dispatcher = filterConfig.getServletContext().getRequestDispatcher(FILTER_PREFIX + ACT_BAD_TOKEN);
			
			}else if(AuthTokenState.REISSUE_TOKEN == state){
				
				dispatcher = filterConfig.getServletContext().getRequestDispatcher(FILTER_PREFIX + ACT_REISSUE_TOKEN);
			}
			else{
				
				dispatcher = filterConfig.getServletContext().getRequestDispatcher(FILTER_PREFIX + ACT_TRAP_ALL);
			}
			
			dispatcher.forward(request, response);
		} catch (ServletException | IOException e) {
			
			LOGGER.error("fail to forward the request", e);
			// ignore
		}
	}
}
