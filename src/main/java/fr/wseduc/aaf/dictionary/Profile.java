package fr.wseduc.aaf.dictionary;

import fr.wseduc.aaf.utils.JsonUtil;
import org.neo4j.graphdb.*;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Profile {

	protected final String id;
	protected final String externalId;
	protected final Importer importer = Importer.getInstance();
	protected JsonObject profile;
	protected final Set<String> functions = Collections.synchronizedSet(new HashSet<String>());
	protected Node node;

	protected Profile(JsonObject profile) {
		this(profile.getString("externalId"), profile);
	}

	protected Profile(JsonObject profile, JsonArray functions) {
		this(profile);
		if (functions != null) {
			for (Object o : functions) {
				if (!(o instanceof String)) continue;
				functions.add(o);
			}
		}
	}

	protected Profile(String externalId, JsonObject struct) {
		if (struct != null && externalId != null && externalId.equals(struct.getString("externalId"))) {
			this.id = struct.getString("id");
		} else {
			throw new IllegalArgumentException("Invalid structure with externalId : " + externalId);
		}
		this.externalId = externalId;
		this.profile = struct;
	}

	private GraphDatabaseService getDb() {
		return importer.getDb();
	}

	public void create() {
		node = getDb().createNode(DynamicLabel.label("Profile"));
		JsonUtil.jsonToNode(profile, node);
	}

	public void createFunctionIfAbsent(String functionExternalId, String name) {
		if (functions.add(functionExternalId)) {
			Node f = getDb().createNode(DynamicLabel.label("Function"));
			f.setProperty("externalId", functionExternalId);
			f.setProperty("id", UUID.randomUUID().toString());
			f.setProperty("name", name);
			f.createRelationshipTo(node, DynamicRelationshipType.withName("COMPOSE"));
		}
	}

}
