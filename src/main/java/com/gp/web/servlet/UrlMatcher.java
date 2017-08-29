package com.gp.web.servlet;

import javax.servlet.http.HttpServletRequest;

public interface UrlMatcher {

	public boolean match(HttpServletRequest request);
}
