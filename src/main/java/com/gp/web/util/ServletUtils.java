package com.gp.web.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class ServletUtils {
	
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
}
