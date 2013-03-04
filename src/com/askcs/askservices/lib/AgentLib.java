package com.askcs.askservices.lib;

import com.almende.eve.agent.AgentFactory;
import com.almende.eve.config.Config;

public class AgentLib {

	private static String CONFIG_FILE = "WEB-INF/eve.yaml";

	private static Config config = null;

	private static Config getConfig() {
		if (config == null) {
			// try to load the default config
			config = new Config();
			try {
				config.load(CONFIG_FILE);
			} catch(Exception ex){
			}
		}
		return config;
	}
	
	public static AgentFactory getAgentFactory() {
		try {
			AgentFactory f = AgentFactory.getInstance(); 
			if (f == null) {
				f = AgentFactory.createInstance(getConfig());
			}
			return f;
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
}
