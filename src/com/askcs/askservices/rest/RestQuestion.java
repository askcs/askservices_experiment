package com.askcs.askservices.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.askcs.askservices.Question;
import com.askcs.askservices.agents.PersonalCapeAgent;
import com.askcs.askservices.auth.SessionHandler;
import com.askcs.util.ParallelInit;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path("/question")
public class RestQuestion {

	/*@GET
	@Produces("application/json")
	public Response getQuestions(@QueryParam("0") String type, @QueryParam("state") String state)
			throws IOException, JSONRPCException {
		
		PersonalAgent agent = SessionHandler.getCurrentAgent();
		ObjectMapper om = ParallelInit.getObjectMapper();
		
		List<Question> questions = null;
		if (type != null && type.equals("sent")) {

			try {
				questions = agent.getOutbox(null, state);
			} catch (Exception ex) {
			}
			
		} else if (type != null && type.equals("all")) {

			try {
				questions = agent.getAllMessages(null,  state);
			} catch (Exception ex) {
			}
		} else if (type != null && type.equals("dm")) {
			
			ObjectNode criteria = om.createObjectNode();
			criteria.put("module", "message");
			
			try {
				questions = agent.getMessagesBy(criteria);
			} catch (Exception ex) {
			}
		} else {
			try {
				questions = agent.getInbox(null, state);
			} catch (Exception ex) {
			}
		}
		
		if(questions!=null) {
			return Response.ok(om.writeValueAsString(questions)).build();
		}

		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	@PUT
	@Path("{uuid}")
	public Response updateQuestion(@PathParam("uuid") String questionUUID,
			String json) throws IOException, JSONRPCException {
		
		ObjectMapper om = ParallelInit.getObjectMapper();
		
		PersonalAgent agent = SessionHandler.getCurrentAgent();
		Question question = agent.updateQuestion(questionUUID, json);
		

		return Response.ok(om.writeValueAsString(question)).build();
	}

	@DELETE
	@Path("/{uuid}")
	public Response delQuestion(@PathParam("uuid") String questionUUID,
			String json) throws IOException, JSONRPCException {
		
		PersonalAgent agent = SessionHandler.getCurrentAgent();
		boolean resp = agent.deleteQuestion(questionUUID);

		return Response.ok(resp + "").build();
	}

	@POST
	public Response createQuestion(String json) throws JsonMappingException,
			IOException, JSONRPCException {
		ObjectMapper om = ParallelInit.getObjectMapper();
		
		PersonalAgent agent = SessionHandler.getCurrentAgent();
		Question question=null;
		try {
			question = agent.send(om.readValue(json, Question.class));
		} catch(Exception e) {
		}

		return Response.ok(om.writeValueAsString(question)).build();
	}*/
}
