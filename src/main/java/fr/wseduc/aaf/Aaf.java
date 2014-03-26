package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@Path("/import")
	public Response importAAF() {
		Importer importer = Importer.getInstance();
		synchronized (this) {
			if (!importer.isReady()) {
				return Response.status(Response.Status.CONFLICT).build();
			}
			importer.init(database);
		}
		Transaction tx = database.beginTx();
		String path = "/home/dboissin/Docs/aaf2d/20130117_fix";
		try {
			new StructureImportProcessing(path).start();
			tx.success();
		} catch (Exception e) {
			tx.failure();
			log.error(e.getMessage(), e);
			return  Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			tx.close();
			importer.clear();
		}
		return Response.status(Response.Status.OK).build();
	}

}
