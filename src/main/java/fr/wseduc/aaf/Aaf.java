package fr.wseduc.aaf;

import org.neo4j.graphdb.GraphDatabaseService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aaf")
public class Aaf {

	private final GraphDatabaseService database;

	public Aaf(@Context GraphDatabaseService database) {
		this.database = database;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/import")
	public Response importAAF(@PathParam("nodeId") long nodeId) {
		return Response.status(Response.Status.OK).build();
	}

}
