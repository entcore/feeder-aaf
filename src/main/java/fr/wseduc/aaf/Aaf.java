package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Aaf {

	private final GraphDatabaseService database;
	private static final Logger log = LoggerFactory.getLogger(Aaf.class);

	public Aaf(@Context GraphDatabaseService database) {
		this.database = database;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/import")
	public Response importAAF(final String json) {
		Importer importer = Importer.getInstance();
		synchronized (this) {
			if (!importer.isReady()) {
				return Response.status(Response.Status.CONFLICT).build();
			}
			importer.init(database);
		}
		String path = new JsonObject(json).getString("path");
		try {
			new StructureImportProcessing(path).start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return  Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			importer.clear();
		}
		return Response.status(Response.Status.OK).build();
	}

}
