package com.askcs.askservices;

import java.io.Serializable;
import java.security.SecureRandom;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;

public class Session implements Serializable{
	
	private static final long serialVersionUID = -7764126841848425634L;
	private static final int SESSION_LEASE_TIME = -1; // Expire time in minutes, -1 is infinite
	
	private String key;
	private String username;
	private String sig;
	private String signed;
	private String handleAssoc;
	private long expireTime;
	
	public Session() {
	}
	
	public Session(String username){
		this(username, null, null, null);
	}
	
	public Session(String username, String sig, String signed,
					String handleAssoc) {
		
		this.key=generateSessionId();
		this.username=username;
		this.sig=sig;
		this.signed=signed;
		this.handleAssoc=handleAssoc;
		
		this.updateExpireTime();
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getSig() {
		return sig;
	}
	
	public String getSigned() {
		return signed;
	}
	
	public String getHandleAssoc() {
		return handleAssoc;
	}
	
	public long getExpireTime() {
		return expireTime;
	}
	
	public boolean isValid() {
		if(expireTime>0) {
		
			DateTime expire = new DateTime(expireTime);
			if(expire.isAfterNow()) {
				updateExpireTime();
				return true;
			}
			
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	public void updateExpireTime() {
		if(SESSION_LEASE_TIME>0) {
			DateTime date = new DateTime();
			date.plusMinutes(SESSION_LEASE_TIME);
			expireTime=date.getMillis();
		} else {
			expireTime=-1;
		}
	}
	
	private String generateSessionId() {
		String sid = username + System.currentTimeMillis();
		sid += getRandom( 256 );
		sid = DigestUtils.sha256Hex(sid);
		return sid;
	}
	
	private byte[] getRandom( int len ) {
		
		byte[] b = new byte[ len ];
		new SecureRandom().nextBytes( b);
		return b;
	}
}
