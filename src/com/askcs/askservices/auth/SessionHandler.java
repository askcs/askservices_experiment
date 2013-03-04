package com.askcs.askservices.auth;

import java.util.HashMap;

import com.almende.eve.agent.AgentFactory;
import com.askcs.askservices.Session;
import com.askcs.askservices.agents.PersonalCapeAgent;
import com.askcs.askservices.lib.AgentLib;

public class SessionHandler {
	
	private static Session currentSession=null;
	private static HashMap<String, Session> sessionMap = new HashMap<String, Session>(); // TODO: make persistant
	private static HashMap<String, Session> sessionUserMap = new HashMap<String, Session>(); // TODO: make persistant

	/**
	 * Create a new session for the user if it doesn't exists and update it if it does.
	 * @param user
	 */
	public static Session createSession(String user) {
		
		Session session;
		if(sessionUserMap.containsKey(user)) {
			
			// If the user ever logged in before
			// reuse the session
			session = sessionUserMap.get(user);			
			session.updateExpireTime();
			
		} else {
			session = new Session(user);				
		}
		
		sessionUserMap.put(user, session);
		sessionMap.put(session.getKey(), session);
		
		currentSession = session;
		
		return session;
	}
	
	public static Session getSession(String sessionID) {
				
		return sessionMap.get(sessionID);		
	}
	
	public static void deleteSession(String sessionID) {
		
		Session session = getSession(sessionID);
		if(session!=null) {
			sessionUserMap.remove(session.getUsername());
			sessionMap.remove(sessionID);
		}
		
		currentSession = null;
	}
	
	public static boolean checkSession(String sessionID) {
		Session session = getSession(sessionID);
		if(session!=null && session.isValid()){
			currentSession=session;
			return true;
		}
		
		currentSession=null;
		return false;
	}
	
	public static String getCurrentAgentId() {
		if(currentSession!=null) {
			return currentSession.getUsername();
		}
		return "";
	}
	
	public static PersonalCapeAgent getCurrentAgent() {
		
		PersonalCapeAgent pa=null;
		try {
			AgentFactory af  = AgentLib.getAgentFactory();
			pa = (PersonalCapeAgent) af.getAgent(getCurrentAgentId());
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return pa;
	}
}
