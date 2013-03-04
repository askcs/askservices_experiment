package com.askcs.askservices.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.askcs.askservices.agents.PersonalCapeAgent;
import com.askcs.askservices.auth.SessionHandler;
import com.askcs.util.ParallelInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/")
public class RestPersonal {
	private static final Logger log = Logger
			.getLogger("AskServices");
	@GET
	@Produces("application/json")
	public Response getPersonalInfo() throws Exception {
		
		log.setLevel(Level.INFO);
		PersonalCapeAgent pa = SessionHandler.getCurrentAgent();
		
		if(pa!=null) {
			log.info("PA found!");
			for(String url : pa.getUrls()) {
				log.info("URL: "+url);
			}
			
			ObjectMapper om = ParallelInit.getObjectMapper();
			String result = om.writeValueAsString(pa);
			pa.destroy();
			
			return Response.ok(result).build();
		}
		
		// This should be impossible!!!
		return Response.status(Status.NOT_FOUND).build();
	}
}
