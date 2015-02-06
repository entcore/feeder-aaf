package fr.wseduc.aaf;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintCreator;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.neo4j.shell.util.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static fr.wseduc.aaf.Util.createPostData;
import static fr.wseduc.aaf.Util.toCypherUri;
import static fr.wseduc.aaf.Util.toJson;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class AafTest {

	private CommunityNeoServer server;
	private static final Logger log = LoggerFactory.getLogger(AafTest.class);

	@Before
	public void before() throws IOException {
		server = CommunityServerBuilder
				.server()
				.onPort(40000)
				.withAutoIndexingEnabledForNodes("externalId")
				.withThirdPartyJaxRsPackage("fr.wseduc.aaf", "/aaf")
				.build();
		server.start();
	}

	@After
	public void after() {
		server.stop();
	}

	@Test
	public void importFiles() {
		Transaction tx = server.getDatabase().getGraph().beginTx();
		ConstraintCreator c = server.getDatabase().getGraph().schema()
				.constraintFor(DynamicLabel.label("Structure"));
		c.assertPropertyIsUnique("id");
		c.assertPropertyIsUnique("externalId");
		c.assertPropertyIsUnique("UAI");
		tx.success();
		tx.close();

		log.info(server.baseUri().toString() + "aaf/import");
		ClientResponse resp = jerseyClient()
				.resource(server.baseUri().toString() + "aaf/import")
				.type("application/json")
				.post(ClientResponse.class, "{\"path\":\"src/test/resources/aaf-test\"}");
		resp.close();

		assertEquals(ClientResponse.Status.OK, resp.getClientResponseStatus());

		String query =
				"MATCH (:User) WITH count(*) as nbUsers " +
				"MATCH (:Structure) WITH count(*) as nbStructures, nbUsers " +
				"MATCH (:Class) WITH nbUsers, nbStructures, count(*) as nbClasses " +
				"MATCH (:FunctionalGroup) WITH nbUsers, nbStructures, nbClasses, count(*) as nbFunctionalGroups " +
				"MATCH (:ProfileGroup) " +
				"RETURN nbUsers, nbStructures, nbClasses, nbFunctionalGroups, count(*) as nbProfileGroups";
		String json = toJson(createPostData(query, null));
		ClientResponse response = Client.create()
				.resource(toCypherUri(server.baseUri().toString()))
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.header("X-Stream", "true")
				.post(ClientResponse.class, json);
		String res = response.getEntity(String.class);
		log.info(res);
		JsonObject result = new JsonObject(res);
		response.close();

		JsonArray r = result.getArray("data").get(0);
		assertEquals(13295, r.get(0));
		assertEquals(10, r.get(1));
		assertEquals(177, r.get(2));
		assertEquals(177, r.get(3));
		assertEquals(177 * 4 + 10 * 4 + 4, r.get(4));

		query = "MATCH (u:User) return HEAD(u.profiles) limit 1";
		json = toJson(createPostData(query, null));
		response = Client.create()
				.resource(toCypherUri(server.baseUri().toString()))
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.header("X-Stream", "true")
				.post(ClientResponse.class, json);
		res = response.getEntity(String.class);
		log.info(res);
		result = new JsonObject(res);
		response.close();
		String type = result.getArray("data").<JsonArray>get(0).get(0);
		assertNotNull(type);
		assertTrue("Personnel".equals(type) || "Teacher".equals(type) ||
				"Student".equals(type) || "Relative".equals(type));
	}

	private Client jerseyClient() {
		DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
		defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
		return Client.create(defaultClientConfig);
	}

}
