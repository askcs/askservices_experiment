package com.askcs.askservices.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("network")
public class RestGroups {

	@GET
	public static Response doGet() {
		String result="[]";
		return Response.ok(result).build();
	}
}
