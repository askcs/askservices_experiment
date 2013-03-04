package com.askcs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class ParallelInit {

	private static ServletContainer restService = new ServletContainer();
	private static FileObjectStore os = new FileObjectStore();
	
	private static Client client = null;	
	private static ObjectMapper om = null;
	
	public static Client getClient() {
		
		if(client==null) {
			
			ClientConfig cc = new DefaultClientConfig();
			cc.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE,10);
			client = Client.create(cc);
			client.setConnectTimeout(1000);
			client.setReadTimeout(15000);
		}
		
		return client;
	}
	
	public static ServletContainer getRestService() {
		
		return restService;
	}
	
	public static ObjectMapper getObjectMapper(){
		if(om==null) {
			om = new ObjectMapper();
			om.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		}
		return om;
	}
	
	public static FileObjectStore getObjectStore() {
		return os;
	}
}
