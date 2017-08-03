/*
 * Licensed to the Ultrabroad Company 
 * 
 */
package com.gp.web.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.WebUtils;

import com.gp.common.AccessPoint;
import com.gp.common.Principal;
import com.gp.common.SystemOptions;
import com.gp.util.ConfigSettingUtils;

/**
 * @author Gary Diao 
 * @version V0.1 2012-07-01
 * */
public abstract class ExWebUtils extends WebUtils{
	
	static Logger LOGGER = LoggerFactory.getLogger(ExWebUtils.class);
	
	/** the principa request attribute key */
	public static final String REQ_ATTR_PRINCIPAL = "_gp_principal_";
	
	/**
	 * Dump the attributes in session to facilitate the development
	 * 
	 * @param HttpServletRequest the request from client.
	 * 
	 * */
	public static void dumpSessionAttributes(HttpServletRequest  request){
		HttpSession session = request.getSession(false);
		if(session != null) {
			Enumeration<String> keys = session.getAttributeNames();
			LOGGER.info("Start Dumping the session attributes ----" );
			while (keys.hasMoreElements())
			{
			  String key = (String)keys.nextElement();
			  LOGGER.info("Attribute KEY : " + key + " - VALUE : " + session.getAttribute(key) );
			}
			LOGGER.info("End Dumping the session attributes ----" );
		}else{
		
			LOGGER.info("session is null!" );
		}
	}

	/**
	 * Dump the attributes in request to facilitate the development
	 * 
	 * @param HttpServletRequest the request from client.
	 * 
	 * */
	public static void dumpRequestAttributes(HttpServletRequest  request){
		
		Enumeration<String> keys = request.getParameterNames();
		
		Cookie[] cookies = request.getCookies();
		
		LOGGER.info("Dumping the request attributes ----" );
		while( keys.hasMoreElements() ) {
			String key = keys.nextElement();

			for( String value : request.getParameterValues( key ) ) {
				LOGGER.info("Attr Name : " + key + " - Value : " + value );
			}
		}
		
		LOGGER.info("Dumping the request headers ----" );
		keys = request.getHeaderNames();
		while( keys.hasMoreElements() ) {
			String key = keys.nextElement();
			Enumeration<String> vals = request.getHeaders(key);
			while( vals.hasMoreElements() ) {
				LOGGER.info("Header Name : " + key + " - Value : " + vals.nextElement() );
			}
		}
		if(cookies != null && cookies.length > 0){
			LOGGER.info("Dumping the request cookies ----" );
			for (Cookie cookie:cookies){
				
				LOGGER.info("Cookie Name : " + cookie.getName() + " - Value : " + cookie.getValue() );
			}
			
		}else {
			
			LOGGER.info("No cookie in request ----" );
		}
	}
	
	/**
	 * Dump the content of request body, it's used for ajax debuging, some time we
	 * need to log out the ajax string carried by http request.
	 * 
	 * Important!!!!: this method is only used for debuging operation. coz it traverse the InputStream
	 * cause the reader index placed on end. if use stream again, it pops IOException;
	 *
	 * @param HttpServletRequest the request object.
	 **/
	public static void dumpRequestBody(HttpServletRequest  request){
		
		ServletServerHttpRequest req = new ServletServerHttpRequest(request);
		try {
			LOGGER.info("Start Dumping the request body content ----" );
			String text = ExWebUtils.getRequestBody(req.getBody());
			LOGGER.info(text);
			LOGGER.info("End Dumping the request body content ----" );
		} catch (IOException e) {

			e.printStackTrace();
		}  
		
	}
	
	/**
	 * Generate and UUID 
	 * @return String 
	 **/
	public static String generateUUID(){
	
		UUID s = UUID.randomUUID();
		return s.toString();
	}
	
	/**
	 * Get the UUID object
	 * @param the uuid string
	 * 
	 * @return the UUID
	 **/
	public static UUID toUUID(String s){
		
		if ( s==null || s.length() ==0){
			LOGGER.debug("The String value is null, can't convert to UUID!");
			return null;
		}
		return UUID.fromString(s);
	}
	
	/**
	 * Get the RequestBody String, default is UTF-8 encoding
	 * 
	 * @param InputStream the inputstream extract from httprequest.
	 * @return String request body content
	 * 
	 **/
	public static String getRequestBody(InputStream  is){
		
		return getRequestBody(is, "UTF-8");
	}

	/**
	 * Get the RequestBody String
	 * 
	 * @param InputStream the inputstream extract from httprequest.
	 * @param coding , UTF-8 etc.
	 * @return String request body content
	 * 
	 **/
	public static String getRequestBody(InputStream  is, String coding){
		
        InputStreamReader input = null;
		try {
			input = new InputStreamReader(is, coding);
		} catch (UnsupportedEncodingException e) {

			//e.printStackTrace();
			LOGGER.debug("getRequestBody UnsupportedEncodingException:",e);
		}
        final int CHARS_PER_PAGE = 5000; //counting spaces
        final char[] buffer = new char[CHARS_PER_PAGE];
        StringBuilder output = new StringBuilder(CHARS_PER_PAGE);
        try {
            for(int read = input.read(buffer, 0, buffer.length);
                    read != -1;
                    read = input.read(buffer, 0, buffer.length)) {
                output.append(buffer, 0, read);
            }
        } catch (IOException ignore) { 
        	
        	LOGGER.debug("getRequestBody IOException",ignore);
        }

        String text = output.toString();
        return text;
	}
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in 
	 * the 200-399 range.
	 * @param url The HTTP URL to be pinged.
	 * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
	 * the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
	 * given timeout, otherwise <code>false</code>.
	 */
	public static boolean ping(String url, int timeout) {
	    url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("HEAD");
	        int responseCode = connection.getResponseCode();
	        return (200 <= responseCode && responseCode <= 399);
	    } catch (IOException exception) {
	        return false;
	    }
	}
	
	/**
	 * Get the access point object for the request
	 * @param request the request from client 
	 **/
	public static AccessPoint getAccessPoint(HttpServletRequest  request){
		
		String client = request.getHeader("User-Agent");
		String host = request.getRemoteHost();
		String app = ConfigSettingUtils.getSystemOption(SystemOptions.SYSTEM_APP);
		String version = ConfigSettingUtils.getSystemOption(SystemOptions.SYSTEM_VERSION);
		
		AccessPoint ap = new AccessPoint(client, host, app, version);
		
		return ap;
	}
	
	/**
	 * Get the ip address of request
	 * @param request the Http Servlet Request from client 
	 **/
    public static String getIpAddr(HttpServletRequest request) {
    	String ip = request.getHeader("x-forwarded-for");      
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
            ip = request.getHeader("Proxy-Client-IP");      
        }      
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
            ip = request.getHeader("WL-Proxy-Client-IP");      
        }      
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
            ip = request.getHeader("HTTP_CLIENT_IP");      
        }      
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");      
        }      
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
            ip = request.getRemoteAddr();      
        }      
        return ip; 
    }

	
	/**
	 * Returns the URL (including query parameters) minus the
	 * scheme, host, and context path. This method probably be moved to a more
	 * general purpose class.
	 */
	public static String getRelativeUrl(HttpServletRequest request) {

		String baseUrl = null;

		if ((request.getServerPort() == 80) || (request.getServerPort() == 443))
			baseUrl = request.getScheme() + "://" + request.getServerName() + request.getContextPath();
		else
			baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();

		StringBuffer buf = request.getRequestURL();

		if (request.getQueryString() != null) {
			buf.append("?");
			buf.append(request.getQueryString());
		}

		return buf.substring(baseUrl.length());
	}

	/**
	 * NOT UNIT TESTED Returns the base url (e.g,
	 * <tt>http://myhost:8080/myapp</tt>) suitable for using in a base tag or
	 * building reliable urls.
	 */
	public static String getBaseUrl(HttpServletRequest request) {
		if ((request.getServerPort() == 80) || (request.getServerPort() == 443))
			return request.getScheme() + "://" + request.getServerName() + request.getContextPath();
		else
			return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
	}

	/**
	 * Returns the file specified by <tt>path</tt> as returned by
	 * <tt>ServletContext.getRealPath()</tt>.
	 */
	public static File getRealFile(HttpServletRequest request, String path) {

		return new File(request.getSession().getServletContext().getRealPath(path));
	}

	/**
	 * Returns the path specified by <tt>path</tt> as returned by
	 * <tt>ServletContext.getRealPath()</tt>.
	 */
	public static String getRealPath(HttpServletRequest request, String path){
		return request.getSession().getServletContext().getRealPath(path);
	}

	/**
	 * Write the image back directly. 
	 * @param response the http response
	 * @param image the the image file 
	 **/
	public static void writeImage(ServletResponse response, File image) throws IOException{

        ByteArrayInputStream iStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(image));
        long length = image.length();
        
        String ext = FilenameUtils.getExtension(image.getPath());
        // Hard-coded for a GIF image - see text.
        response.setContentType("image/" + ext);
        response.setContentLength((int)length);

        ServletOutputStream oStream = response.getOutputStream();
        try{
	        byte [] buffer = new byte[1024];
	        int len;
	        while ((len = iStream.read(buffer)) != -1) {
	            oStream.write(buffer, 0, len);
	        }
	        oStream.flush();
        }finally{
	        iStream.close();
	        oStream.close();
        }
	}
	
	/**
	 * Append the principal object in request
	 * 
	 * @param request the request from client
	 * @param principal the user principal 
	 * 
	 **/
	public static void setPrincipal(HttpServletRequest request, Principal principal){
		
		if(null == request) return;
		
		request.setAttribute(REQ_ATTR_PRINCIPAL, principal);
	}
	
	/**
	 * Fetch the principal object out of request attribute.
	 * 
	 * @param request the request from client
	 * 
	 **/
	public static Principal getPrincipal(HttpServletRequest request){
		
		return (Principal) request.getAttribute(REQ_ATTR_PRINCIPAL);
	}
	
}