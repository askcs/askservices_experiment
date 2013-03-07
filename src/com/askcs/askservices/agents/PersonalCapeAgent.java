package com.askcs.askservices.agents;

import java.util.logging.Logger;

import com.almende.cape.agent.CapeClientAgent;
import com.almende.cape.handler.NotificationHandler;
import com.almende.eve.agent.annotation.Name;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PersonalCapeAgent extends CapeClientAgent {
	private static final Logger log = Logger
			.getLogger(PersonalCapeAgent.class.toString());
	
	
	@Override
	public void init() {
		super.init();
		
		try {
			setNotificationHandler(new NotificationHandler() {
				
				@Override
				public void onNotification(String message) {
					// TODO Auto-generated method stub
					System.out.println(message);
					
					// forward to phone?
					try {
						sendClientNotification(getId(), message);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch(Exception e) {
		}
	}
	
	public void sendClientNotification(@Name("userId") String userId,@Name("message") String message) throws Exception {
		if (userId == null) {
			userId = getId();
		}
		String dataType = "client";
		
		// find an agent which can handle a dialog with the user
		String notificationAgentUrl = findDataSource(userId, dataType);
		if (notificationAgentUrl == null) {
			throw new Exception(
					"No data source found supporting a dialog with user " + userId);
		}
		
		// send the notification
		String method = "onNotification";
		ObjectNode params = JOM.createObjectNode();
		params.put("message", message);
		send(notificationAgentUrl, method, params);
	}
	
	@Override
	public void setAccount(String username, String password, String resource)
			throws Exception {
		removeAccount(getUsername(), getPassword());
		super.setAccount(username, password, resource);
		
		try {
			connect();
		} catch (Exception e){
			log.warning("Failed to login: "+getUsername()+" err: "+e.getMessage());
		}
		
		try {
			register("dialog");
		} catch (Exception e){
			log.warning("Failed to register to dialog err: "+e.getMessage());
		}		
		
		register("contacts");
		register("state");		
		
	}
	
	// [start] Extra getters and setters
	// JSON serializable properties
	@JsonProperty
	public String getName() {
		return (String) getState().get("name");
	}
	@JsonProperty
	public void setName(String name) {
		getState().put("name", name);
	}
	@JsonProperty
	public String getPhone() {
		return (String) getState().get("phone");
	}
	public void setPhone(String phone) {
		getState().put("phone", phone);
	}
	@JsonProperty
	public String getEmail() {
		return (String) getState().get("email");
	}
	public void setEmail(String email) {
		getState().put("email", email);
	}
	
	public String getVeriCode() {
		return (String) getState().get("veriCode");
	}
	public void setVeriCode(String veriCode) {
		getState().put("veriCode", veriCode);
	}
	// [end] Extra getters and setters
	
	@Override
	public String getDescription() {
		return "The Personal Agent is your digital secretary. " + 
				"Its purpose is to make your digital life easier.";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}
}
