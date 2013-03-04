package com.askcs.askservices.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.almende.eve.agent.AgentFactory;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;

import com.askcs.askservices.agents.PersonalCapeAgent;
import com.askcs.askservices.agents.StateAgent;
import com.askcs.askservices.auth.SessionHandler;


// NB: input in seconds, output in milliseconds 
@Path("/slot")
public class RestPersonalState {

	static boolean REMOTE_FORWARD = false;
	
	
	@GET
	@Produces("application/json")
	public static Response doGet( @QueryParam("start") int start, @QueryParam("end") int end, @QueryParam("recurring") boolean recurring ) 
	{
		PersonalCapeAgent agent = SessionHandler.getCurrentAgent();
		
		String type = "ONCE";
		if(recurring)type = "WEEKLY";
		
		String calendarAgentUrl = (String) agent.getContext().get("stateAgent_url");
		if (calendarAgentUrl == null) 
		{
			return Response.ok("[]").build();
		}
		
//System.out.println("GET("+start+" "+end+" "+recurring+") forward: "+ calendarAgentUrl );

		String result = "[]";

		//forward call
		String method = "slots_get";
		ObjectNode params = JOM.createObjectNode();
		params.put("start_millis", start*1000L );
		params.put("end_millis", end*1000L );
		params.put("occurence", type );
		try{
			ArrayList<String> node = agent.send(calendarAgentUrl, method, params, ArrayList.class); // ObjectNode
			ObjectMapper om = new ObjectMapper();
			result = om.writeValueAsString( node );
		}catch(Exception E)
		{
			System.err.println("failed to send: "+ E.toString() );
		}
		
		return Response.ok(result).build();
	}	
	
	@POST
	@Produces("application/json")
	public static Response doPost( String json) 
	{
		//bodyParams
		int start=0;
		int end=0;
		boolean recurring=false;
		String value = null;
		
		try{
			ObjectMapper om = new ObjectMapper();
			HashMap<String,Object> bodyParam = om.readValue(json, HashMap.class );
			start = (Integer)bodyParam.get("start");
			end = (Integer)bodyParam.get("end");
			recurring = (Boolean)bodyParam.get("recurring");
			value = (String)bodyParam.get("value");
		}
		catch( IOException ioe)
		{
			System.err.println("fail");
			return Response.status(400).build();
		}

		String type = "ONCE";
		if(recurring)type = "WEEKLY";
		
//System.out.println("POST("+start+" "+end+" "+recurring+" "+value+") forward: "+ calendarAgentUrl +" "+ calendarAgentId );
		
		boolean result = create_slot( start,end, value, type );
		if( result )return Response.ok( "true" ).build();
		return Response.status(400).build();
	}
	
	@PUT	//note: new slot in query, old slot in body
	@Produces("application/json")
	public static Response doPut( @QueryParam("start") int start, @QueryParam("end") int end, @QueryParam("recurring") boolean recurring, @QueryParam("value") String value, String json )
	{
		try{
			ObjectMapper om = new ObjectMapper();
			HashMap<String,Object> bodyParam = om.readValue(json, HashMap.class );
			int old_start = (Integer)bodyParam.get("start");
			int old_end = (Integer)bodyParam.get("end");
			boolean old_recurring = (Boolean)bodyParam.get("recurring");
			//String old_value = (String)bodyParam.get("value");
			
			String old_type = "ONCE";
			if(old_recurring)old_type = "WEEKLY";
			
			create_slot( old_start,old_end, null, old_type ); // null means Delete 
		}
		catch( IOException ioe)
		{
		}
		
		String type = "ONCE";
		if(recurring)type = "WEEKLY";
		
//System.out.println("PUT("+start+" "+end+" "+recurring+" "+value+") forward: "+ calendarAgentUrl +" "+ calendarAgentId );
		
		boolean result = create_slot( start,end, value, type );
		if( result )return Response.ok( "true" ).build();
		return Response.status(400).build();
	}

	@DELETE //TODO: remove slot
	@Produces("application/json")
	public static Response doDelete( @QueryParam("start") int start, @QueryParam("end") int end, @QueryParam("recurring") boolean recurring )
	{
		String type = "ONCE";
		if(recurring)type = "WEEKLY";
		
//System.out.println("DELETE("+start+" "+end+" "+recurring+" "+value+") forward: "+ calendarAgentUrl +" "+ calendarAgentId );
		
		boolean result = create_slot( start,end, null, type );	// null means Delete
		if( result )return Response.ok( "true" ).build();
		return Response.status(400).build();
	}
	
	
	
	/////////////////////////
	
	private static boolean create_slot( int start, int end, String value, String type)
	{
		PersonalCapeAgent agent = SessionHandler.getCurrentAgent();
		
		String calendarAgentUrl = (String) agent.getContext().get("stateAgent_url");
		String calendarAgentId  = (String) agent.getContext().get("stateAgent_id");
		if (calendarAgentUrl == null) 
		{
			try{
				String id = UUID.randomUUID().toString();
				AgentFactory af = agent.getAgentFactory();
				StateAgent sa = (StateAgent)af.createAgent(StateAgent.class, id);
				calendarAgentUrl = sa.getUrls().get(0);
				calendarAgentId = sa.getId();
				sa.destroy();
				agent.getContext().put("stateAgent_url", calendarAgentUrl);
				agent.getContext().put("stateAgent_id", calendarAgentId );
				agent.destroy();
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		boolean result = false;

		if( REMOTE_FORWARD || calendarAgentId==null )
		{
			String method = "slots_put";
			ObjectNode params = JOM.createObjectNode();
			params.put("start_millis", start*1000L );
			params.put("end_millis", end*1000L );
			params.put("description", value);
			params.put("occurence", type );
			try{
				result = agent.send(calendarAgentUrl, method, params, boolean.class); // ObjectNode
				agent.destroy();
			}catch(Exception e)
			{
				System.err.println("failed to send: "+ e.toString() );
			}
		}
		else
		{
			try{
				AgentFactory af = agent.getAgentFactory();
				StateAgent sa = (StateAgent)af.getAgent( calendarAgentId );
				result= sa.slots_put( start*1000L, end*1000L, value, type );
				sa.destroy(); //persist?
			}catch(Exception e)
			{
				System.err.println("failed to call: "+ e.toString() );
			}
		}
		
		return result;
	}
	
}
