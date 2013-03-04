package com.askcs.askservices.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.*;

import com.askcs.askservices.auth.AuthenticationService;
import com.askcs.util.ParallelInit;
import com.sun.jersey.spi.container.servlet.ServletContainer;

@SuppressWarnings("serial")
public class RestServlet extends HttpServlet {
	
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		// some CORS stuff
		if(addCORSHeaders(req, resp))
			return;
		
		if(this.authenticationService(req, resp))
			this.jerseyService(req, resp);
	}
	
	/**
	 * Add CORS headers. If call is OPTIONS call, send ok. 
	 * @param req
	 * @param resp
	 */
	private boolean addCORSHeaders(HttpServletRequest req, HttpServletResponse resp)
	{
		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = (Enumeration<String>) req.getHeaderNames();
		HashMap<String, String> httpHeaders = new java.util.HashMap<String, String>();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement().toUpperCase(); // avoid weird caseSensitive problem
			httpHeaders.put(name, req.getHeader(name));
		}
		HashMap<String,String> ret = new HashMap<String,String>();
		ret.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		ret.put("Access-Control-Allow-Credentials", "true" );
		ret.put("Access-Control-Max-Age", "60" );
		if( httpHeaders.get("ORIGIN") != null )
			ret.put("Access-Control-Allow-Origin", httpHeaders.get("ORIGIN") );
		if( httpHeaders.get("ACCESS-CONTROL-REQUEST-HEADERS") != null )
			ret.put("Access-Control-Allow-Headers", httpHeaders.get("ACCESS-CONTROL-REQUEST-HEADERS") );
		
		for (java.util.Map.Entry<String, String> entry : ret.entrySet())
			resp.setHeader(entry.getKey(), entry.getValue());
		
		if( req.getMethod().equals("OPTIONS") )
		{
			resp.setStatus(200);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Function which uses the AuthenticationService to check if it is an authentication call.
	 * If not, it will check if the call is authorized.
	 * @param req
	 * @param res
	 * @return Return true if the call has not been processed.
	 */
	private boolean authenticationService(HttpServletRequest req, HttpServletResponse res){
		
		AuthenticationService as = new AuthenticationService();
		return as.authenticate(req, res);
	}
	
	private void jerseyService(HttpServletRequest req, HttpServletResponse res){
		
		try{
			// perform jersey with a copied config
			ServletConfig configCopy = this.getServletConfig();
			
			ServletContainer co = ParallelInit.getRestService();
			co.init(configCopy);
			co.service(req, res);
			co.destroy();
		}
		catch(Exception e)
		{
			try{
				e.printStackTrace();
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
			}catch(IOException ioe){}
			return;
		}
	}
}