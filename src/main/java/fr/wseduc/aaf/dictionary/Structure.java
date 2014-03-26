package fr.wseduc.aaf.dictionary;

import fr.wseduc.aaf.utils.JsonUtil;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.*;

import static org.neo4j.graphdb.DynamicLabel.label;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

public class Structure {

	protected final String id;
	protected final String externalId;
	protected final Importer importer = Importer.getInstance();
	protected JsonObject struct;
	protected final Set<String> classes = Collections.synchronizedSet(new HashSet<String>());
	protected final Set<String> functionalGroups = Collections.synchronizedSet(new HashSet<String>());
	protected Node node;
	protected final HashMap<String, Node> profileGroups = new HashMap<>();

	protected Structure(JsonObject struct) {
		this(struct.getString("externalId"), struct);
	}

	protected Structure(JsonObject struct, JsonArray groups, JsonArray classes) {
		this(struct);
		if (groups != null) {
			for (Object o : groups) {
				if (!(o instanceof String)) continue;
				functionalGroups.add((String) o);
			}
		}
		if (classes != null) {
			for (Object o : classes) {
				if (!(o instanceof String)) continue;
				this.classes.add((String) o);
			}
		}
	}

	protected Structure(String externalId, JsonObject struct) {
		if (struct != null && externalId != null && externalId.equals(struct.getString("externalId"))) {
			this.id = struct.getString("id");
		} else {
			throw new IllegalArgumentException("Invalid structure with externalId : " + externalId);
		}
		this.externalId = externalId;
		this.struct = struct;
	}

	private GraphDatabaseService getDb() {
		return importer.getDb();
	}

	public void create() {
		node = getDb().createNode(label("Structure"));
		JsonUtil.jsonToNode(struct, node);
		ResourceIterable<Node> profiles =  GlobalGraphOperations.at(getDb())
				.getAllNodesWithLabel(label("Profile"));
		for (Node p : profiles) {
			Node g = getDb().createNode(label("Group"), label("ProfileGroup"));
			g.setProperty("name", node.getProperty("name") + "-" + p.getProperty("name"));
			g.setProperty("id", UUID.randomUUID().toString());
			g.createRelationshipTo(p, withName("HAS_PROFILE"));
			g.createRelationshipTo(node, withName("DEPENDS"));
			profileGroups.put(p.getProperty("externalId").toString(), g);
		}
	}

	public void addAttachment() {
		JsonArray functionalAttachment = struct.getArray("functionalAttachment");
		if (functionalAttachment != null && functionalAttachment.size() > 0 &&
				!externalId.equals(functionalAttachment.get(0))) {
			ResourceIterable<Node> structures = getDb().findNodesByLabelAndProperty(label("Structure"),
					"externalId", functionalAttachment.toArray());
			for (Node n : structures) {
				node.createRelationshipTo(n, withName("HAS_ATTACHMENT"));
			}
		}
	}

	public void createClassIfAbsent(String classExternalId, String name) {
		if (classes.add(classExternalId)) {
			Node c = getDb().createNode(label("Class"));
			c.setProperty("id", UUID.randomUUID().toString());
			c.setProperty("externalId", classExternalId);
			c.setProperty("name", name);
			c.createRelationshipTo(node, withName("BELONGS"));
			for (Node n : profileGroups.values()) {
				Relationship rp = n.getSingleRelationship(withName("HAS_PROFILE"), Direction.OUTGOING);
				Node p = rp.getEndNode();
				Node cg = getDb().createNode(label("ProfileGroup"), label("Group"));
				cg.setProperty("name", c.getProperty("name") + "-" + p.getProperty("name"));
				cg.setProperty("id", UUID.randomUUID().toString());
				cg.createRelationshipTo(n, withName("DEPENDS"));
				cg.createRelationshipTo(c, withName("DEPENDS"));
			}
		}
	}

	public void createFunctionalGroupIfAbsent(String groupExternalId, String name) {
		if (functionalGroups.add(groupExternalId)) {
			Node g = getDb().createNode(label("Group"), label("FunctionalGroup"));
			g.setProperty("id", UUID.randomUUID().toString());
			g.setProperty("externalId", groupExternalId);
			g.setProperty("name", name);
			g.createRelationshipTo(node, withName("DEPENDS"));
		}
	}

//	public void linkModules(String moduleExternalId) {
//		String query =
//				"MATCH (s:Structure { externalId : {externalId}}), " +
//				"(m:Module { externalId : {moduleExternalId}}) " +
//				"CREATE UNIQUE s-[:OFFERS]->m";
//		JsonObject params = new JsonObject()
//				.putString("externalId", externalId)
//				.putString("moduleExternalId", moduleExternalId);
//		getTransaction().add(query, params);
//	}
//
//	public void linkClassFieldOfStudy(String classExternalId, String fieldOfStudyExternalId) {
//		String query =
//				"MATCH (s:Structure { externalId : {externalId}})" +
//				"<-[:BELONGS]-(c:Class { externalId : {classExternalId}}), " +
//				"(f:FieldOfStudy { externalId : {fieldOfStudyExternalId}}) " +
//				"CREATE UNIQUE c-[:TEACHES]->f";
//		JsonObject params = new JsonObject()
//				.putString("externalId", externalId)
//				.putString("classExternalId", classExternalId)
//				.putString("fieldOfStudyExternalId", fieldOfStudyExternalId);
//		getTransaction().add(query, params);
//	}
//
//	public void linkGroupFieldOfStudy(String groupExternalId, String fieldOfStudyExternalId) {
//		String query =
//				"MATCH (s:Structure { externalId : {externalId}})" +
//				"<-[:DEPENDS]-(c:FunctionalGroup { externalId : {groupExternalId}}), " +
//				"(f:FieldOfStudy { externalId : {fieldOfStudyExternalId}}) " +
//				"CREATE UNIQUE c-[:TEACHES]->f";
//		JsonObject params = new JsonObject()
//				.putString("externalId", externalId)
//				.putString("groupExternalId", groupExternalId)
//				.putString("fieldOfStudyExternalId", fieldOfStudyExternalId);
//		getTransaction().add(query, params);
//	}

	public String getExternalId() {
		return externalId;
	}

	public Node getNode() {
		return node;
	}

	public Node getProfileGroup(String profileExternalId) {
		return profileGroups.get(profileExternalId);
	}

}
