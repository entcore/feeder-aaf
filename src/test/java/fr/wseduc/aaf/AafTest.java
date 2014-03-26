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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;


public class AafTest {

	private CommunityNeoServer server;
	private static final Logger log = LoggerFactory.getLogger(AafTest.class);

	@Before
	public void before() throws IOException {
		server = CommunityServerBuilder
				.server()
				.onPort(40000)
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
		ClientResponse.Status status = jerseyClient()
				.resource(server.baseUri().toString() + "aaf/import")
				.post(ClientResponse.class).getClientResponseStatus();

		assertEquals(ClientResponse.Status.OK, status);
	}

	private Client jerseyClient() {
		DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
		defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
		return Client.create(defaultClientConfig);
	}

}
