package fr.wseduc.aaf;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;


public class AafTest {

	private CommunityNeoServer server;
	private static final Logger log = LoggerFactory.getLogger(AafTest.class);

	@Before
	public void before() throws IOException {
		server = CommunityServerBuilder
				.server()
				.onPort(40000)
				.withThirdPartyJaxRsPackage("fr.wseduc.aaf", "/wse")
				.build();
		server.start();
	}

	@After
	public void after() {
		server.stop();
	}

	@Test
	public void importFiles() {
		log.info(server.baseUri().toString() + "wse/aaf/import");
		ClientResponse.Status status = jerseyClient()
				.resource(server.baseUri().toString() + "wse/aaf/import")
				.post(ClientResponse.class).getClientResponseStatus();

		assertEquals(ClientResponse.Status.OK, status);
	}

	private Client jerseyClient() {
		DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
		defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
		return Client.create(defaultClientConfig);
	}

}
