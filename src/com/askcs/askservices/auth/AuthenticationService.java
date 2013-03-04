package com.askcs.askservices.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.almende.eve.agent.AgentFactory;
import com.askcs.askservices.Session;
import com.askcs.askservices.agents.PersonalCapeAgent;
import com.askcs.askservices.lib.AgentLib;
import com.askcs.util.ParallelInit;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

public class AuthenticationService {
	private static final Logger log = Logger
			.getLogger(AuthenticationService.class.toString());
	private static final String OPENID_SERVER = "https://openid.ask-cs.com/openid/";
	
	private static final String VERIFICATION_TYPE_NONE = "none";
	private static final String VERIFICATION_TYPE_PHONE = "phone";
	private static final String VERIFICATION_TYPE_SMS = "sms";
	private static final String VERIFICATION_TYPE_EMAIL = "email";
	
	private HttpServletRequest req;
	private HttpServletResponse res;
	
	public AuthenticationService() {
		log.setLevel(Level.INFO);
	}
	
	/**
	 * Check if the request is authenticated or needs authenticating.
	 * @param req
	 * @param res
	 * @return Returns False if the request has been handled. True if the session is valid and can continue with the request.
	 */
	public boolean authenticate(HttpServletRequest req, HttpServletResponse res) {
		
		this.req=req;
		this.res=res;
		
		String path = req.getRequestURI().substring(1).split("/")[0];
		
		if(path.toLowerCase().startsWith("askfast")) {
			return true;
		} else if(path.toLowerCase().equals("login")) {
			handleLogin();
			return false;
		} else if(path.toLowerCase().equals("openidlogin")) {
			try {
				handleOpenIDLogin();
			} catch(Exception ex) {
			}
			return false;
		} else if(path.toLowerCase().equals("openidlogin_ret")) {
			handleLoginResult();
			return false;
		} else if(path.toLowerCase().equals("register")) {
			handleRegister();
			return false;
		} else if(path.toLowerCase().equals("register_verify")) {
			handleRegisterVerify();
			return false;
		} else if(path.toLowerCase().equals("unregister")) {
			handleUnregister();
			return false;
		} if(path.toLowerCase().equals("logout")) {
			handleLogout();
			return false;
		} else {
			if(!checkSession()) {
				try{
					res.sendError(HttpServletResponse.SC_FORBIDDEN);
					return false;
				} catch(Exception ex){
				}
			}
			return true;
		}
	}
	
	/**
	 * Normal login check, so based on username and password
	 * Required query params are: username, password
	 */
	private void handleLogin() {
		
		String user = req.getParameter("username");
		String pass= req.getParameter("password");
		if(user==null || user.equals("") || pass==null || pass.equals("")) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		AgentFactory af = AgentLib.getAgentFactory();
		try {
			PersonalCapeAgent pa = (PersonalCapeAgent) af.getAgent(user);
			if(pa!=null) {
				//if(pa.getPassword()!=null && pa.getPassword().equals(pass)) {
					loginOK(user);
					return;
				//}
			}
			
			log.info("Login Failed! Wrong password from: PersonalAgent: "+user);
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch(Exception ex){
			}
			return;
		} catch(Exception ex) {
			log.warning("Failed to load PersonalAgent: "+user);
			ex.printStackTrace();
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch(Exception e){
			}
		}
	}
	
	/**
	 * Open ID login check, this will redirect to ASK CS openid server
	 * @throws UnsupportedEncodingException
	 */
	private void handleOpenIDLogin() throws UnsupportedEncodingException {
		
		String s = OPENID_SERVER;
		String user = req.getParameter("username");
		if(user==null) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		Client client = ParallelInit.getClient();
		
		// Get the handle assoc
		WebResource wr = client.resource(OPENID_SERVER);
		String result = wr.queryParam("openid.mode", "associate").get(String.class);
		
		String assocHandle="";
		String[] lines = result.split("\n");
		for(String line : lines) {
			String[] keyVal = line.split(":");
			if(keyVal[0].equals("assoc_handle")) {
				assocHandle=keyVal[1];
				break;
			}
		}
		
		s += user+"?openid.mode=checkid_setup";
		s += "&openid.ns=http://specs.openid.net/auth/2.0";
		s += "&openid.identity="+URLEncoder.encode(OPENID_SERVER+user, "UTF8");
		s += "&openid.claimed_id="+URLEncoder.encode(OPENID_SERVER+user,"UTF8");
		s += "&openid.realm=http://"+req.getRemoteHost();
		s += "&openid.assoc_handle="+assocHandle;
		s += "&openid.return_to="+req.getRequestURL().toString()+"_ret";
		
		String ret = req.getParameter("retpath");
		if (ret != null && !ret.equals("")) {
			s += "?retpath=" + ret;
		}
		
		try {
			String pass = req.getParameter("password");
			if(pass!=null) {
				String token = user+":"+pass;
				byte[] encoded = Base64.encode(token.getBytes());
				token = new String(encoded);
				res.setHeader("Authorization", "Basic "+token);
			}
			res.sendRedirect(s);
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void handleLoginResult() {
		
		String mode = req.getParameter("openid.mode");
		if(mode.equals("id_res")) {
			// Create Session
			String identity = req.getParameter("openid.identity");
			/*String assocHandle = req.getParameter("openid.assoc_handle");
			String signed = req.getParameter("openid.signed");
			String sig = req.getParameter("openid.sig");*/
			
			String user = identity.replace(OPENID_SERVER, "");
			
			@SuppressWarnings("unused")
			String ret = req.getParameter("retpath");
			// TODO: implement return path functionality
			
			// Check if user has agent
			try {
				AgentFactory af = AgentLib.getAgentFactory();
				if(af.hasAgent(user)) {
					loginOK(user);
				}
			} catch (Exception ex) {
				
				log.warning("Failed to check if personal agent exists");
				try {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch(Exception e){
				}
			}
		}
		
		try {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch(Exception ex){
		}
	}
	
	private void loginOK(String user){
		
		Session session = SessionHandler.createSession(user);
		
		Cookie cookie = new Cookie("X-SESSION_ID", session.getKey());
		res.addCookie(cookie);

		res.addHeader("X-SESSION_ID", session.getKey());

		try {
			ServletOutputStream out = res.getOutputStream();
			out.println("{\"X-SESSION_ID\":\"" + session.getKey() + "\"}");
		} catch (Exception ex){
		}
		res.setContentType("application/json");
	}
	
	private void handleRegister() {
		
		String user = req.getParameter("username");
		String pass = req.getParameter("password");
		String phone = req.getParameter("phone");
		String email = req.getParameter("email");
		String code=null;
		String verification = req.getParameter("verification");
		if(user==null || user.equals("") ||
				pass==null || pass.equals("") ||
			verification==null || verification.equals("")) {
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch(Exception ex){
			}
			return;
		}
		
		if(verification.toLowerCase().equals(VERIFICATION_TYPE_PHONE)) {
			
			if(phone==null || phone.equals("")) {
				try {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch(Exception ex){
				}
				return;
			}
			
			// TODO: Create code and send and call to the user
		} else if(verification.toLowerCase().equals(VERIFICATION_TYPE_SMS)) {
			if(phone==null || phone.equals("")) {
				try {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch(Exception ex){
				}
				return;
			}
			
			// TODO: Create code and send it via sms
		} else if(verification.toLowerCase().equals(VERIFICATION_TYPE_EMAIL)) {
			if(email==null || email.equals("")) {
				try {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch(Exception ex){
				}
				return;
			}
			
			// TODO: Create code and send it via email
		} else if(verification.toLowerCase().equals(VERIFICATION_TYPE_NONE)) {
			// Don't do anything, user can just be registered.
		} else {
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsuported verification type");
			} catch(Exception ex){
			}
			return;
		}
		
		if(!registerUser(user, pass, phone, email, code)) {
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch(Exception ex){
			}
			return;
		}
		
		loginOK(user);
	}
	
	private void handleRegisterVerify() {
		// TODO: implement this function
	}
	
	private void handleUnregister() {
		
		String sid = this.getSidFromRequest();
		if(sid==null) {
			try {
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			} catch(Exception ex){
			}
		}
		
		Session session = SessionHandler.getSession(sid);
		
		try {
			AgentFactory af = AgentLib.getAgentFactory();			
			af.deleteAgent(session.getUsername());
			log.info("Unregistered PersonalAgent: "+session.getUsername());
		} catch (Exception ex) {
			log.warning("Failed to delete PersonalAgent of: "+session.getUsername());
			ex.printStackTrace();
		}
		
		handleLogout();
	}
	
	private boolean registerUser(String user, String pass, String phone, String email, String code) {
		
		AgentFactory af = AgentLib.getAgentFactory();
		PersonalCapeAgent agent = null;
		try {
			agent = (PersonalCapeAgent) af.createAgent(PersonalCapeAgent.class, user);
			//agent.delete();
		} catch(Exception ex) {
			log.warning("(Regiser) Failed to create agent");
			ex.printStackTrace();
			return false;
		}
		
		if(phone!=null)
			agent.setPhone(phone);
		
		if(email!=null)
			agent.setEmail(email);
		
		if(code!=null) {
			agent.setVeriCode( DigestUtils.md5Hex(pass) +"|"+code );
		} else {
			try {
				agent.setAccount(user, pass, null);
			} catch(Exception ex){
				log.warning("Failed to login: "+agent.getUsername()+" err: "+ex.getMessage());
				return false;
			}
			//agent.setPassword( DigestUtils.md5Hex(pass));
		}
		
		log.info("Registered PersonalAgent: "+user+" pass: "+pass);
		agent.destroy();
		return true;
	}
	
	private void handleLogout() {
		
		String sid = this.getSidFromRequest();
		if(sid!=null) {
			SessionHandler.deleteSession(sid);
		}
		
		Cookie cookie = new Cookie("X-SESSION_ID", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		res.addCookie(cookie);
	}
	
	private boolean checkSession() {
		
		String sid = this.getSidFromRequest();
		if(sid!=null){
			
			if(SessionHandler.checkSession(sid)) {
				return true;
			}
		}
		
		return false;
	}
	
	private String getSidFromRequest() {
		String sid = req.getHeader("X-SESSION_ID");
		if (sid != null)
			return sid;

		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("X-SESSION_ID")) {
				sid = cookie.getValue();
				break;
			}
		}
		return sid;
	}
}
