package fr.wseduc.aaf.dictionary;

import fr.wseduc.aaf.utils.JsonUtil;
import fr.wseduc.aaf.utils.Validator;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.neo4j.graphdb.DynamicLabel.label;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

public class Importer {

	private static final Logger log = LoggerFactory.getLogger(Importer.class);
	private final ConcurrentMap<String, Structure> structures = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Profile> profiles = new ConcurrentHashMap<>();
	private GraphDatabaseService db;
	private final Validator structureValidator;
	private final Validator profileValidator;
	private final Validator studyValidator;
	private final Validator moduleValidator;
	private final Validator userValidator;
	private final Validator personnelValidator;
	private final Validator studentValidator;

	private Importer() {
		structureValidator = new Validator("dictionary/schema/Structure.json");
		profileValidator = new Validator("dictionary/schema/Profile.json");
		studyValidator = new Validator("dictionary/schema/FieldOfStudy.json");
		moduleValidator = new Validator("dictionary/schema/Module.json");
		userValidator = new Validator("dictionary/schema/User.json");
		personnelValidator = new Validator("dictionary/schema/Personnel.json");
		studentValidator = new Validator("dictionary/schema/Student.json");
	}

	private static class StructuresHolder {
		private static final Importer instance = new Importer();
	}

	public static Importer getInstance() {
		return StructuresHolder.instance;
	}

	public void init(final GraphDatabaseService db) {
		this.db = db;
	}


	public GraphDatabaseService getDb() {
		return db;
	}

	public void clear() {
		structures.clear();
		profiles.clear();
		db = null;
	}

	public boolean isReady() {
		return db == null;
	}

	public Structure createStructure(JsonObject struct) {
		final String error = structureValidator.validate(struct);
		Structure s = null;
		if (error != null) {
			log.warn(error);
		} else {
			String externalId = struct.getString("externalId");
			s = structures.get(externalId);
			if (s == null) {
				try {
					s = new Structure(externalId, struct);
					Structure old = structures.putIfAbsent(externalId, s);
					if (old == null) {
						s.create();
					}
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage());
				}
			}
		}
		return s;
	}

	public Profile createProfile(JsonObject profile) {
		final String error = profileValidator.validate(profile);
		Profile p = null;
		if (error != null) {
			log.warn(error);
		} else {
			String externalId = profile.getString("externalId");
			p = profiles.get(externalId);
			if (p == null) {
				try {
					p = new Profile(externalId, profile);
					Profile old = profiles.putIfAbsent(externalId, p);
					if (old == null) {
						p.create();
					}
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage());
				}
			}
		}
		return p;
	}

	public void createFieldOfStudy(JsonObject object) {
		final String error = studyValidator.validate(object);
		if (error != null) {
			log.warn(error);
		} else {
			Node n = getDb().createNode(label("FieldOfStudy"));
			JsonUtil.jsonToNode(object, n);
		}
	}

	public void createModule(JsonObject object) {
		final String error = moduleValidator.validate(object);
		if (error != null) {
			log.warn(error);
		} else {
			Node n = getDb().createNode(label("Module"));
			JsonUtil.jsonToNode(object, n);
		}
	}

	public void createUser(JsonObject object) {
		final String error = userValidator.validate(object);
		if (error != null) {
			log.warn(error);
		} else {
			Node n = getDb().createNode(label("User"));
			JsonUtil.jsonToNode(object, n);
		}
	}

	public void createPersonnel(JsonObject object, String profileExternalId,
			Set<String> structuresByFunctions, String[][] linkClasses,
			String[][] linkGroups) {
		final String error = personnelValidator.validate(object);
		if (error != null) {
			log.warn(error);
		} else {
			Node u = getDb().createNode(label("User"));
			JsonUtil.jsonToNode(object, u);

			JsonArray structures = object.getArray("structures");
			if (structures != null && structures.size() > 0) {
				for (Object o : structures) {
					if (!(o instanceof String)) continue;
					Structure s = this.structures.get(o);
					if (s != null) {
						Node structure = s.getNode();
						if (structure != null) {
							u.createRelationshipTo(structure, withName("ADMINISTRATIVE_ATTACHMENT"));
						}
					}
				}
			}

			if (structuresByFunctions != null && structuresByFunctions.size() > 0) {
				for (String o : structuresByFunctions) {
					Structure s = this.structures.get(o);
					if (s != null) {
						Node spg = s.getProfileGroup(profileExternalId);
						if (spg != null) {
							u.createRelationshipTo(spg, withName("IN"));
						}
					}
				}
			}

			if (linkClasses != null) {
				for (String[] structClass : linkClasses) {
					if (structClass != null && structClass[0] != null && structClass[1] != null) {
						Structure s = this.structures.get(structClass[0]);
						if (s != null) {
							Node spg = s.getProfileGroup(profileExternalId);
							if (spg != null) {
								Node cpg = null;
								pgl: for (Relationship r : spg.getRelationships(withName("DEPENDS"),
										Direction.INCOMING)) {
									cpg = r.getStartNode();
									for (Relationship r2 : cpg.getRelationships(withName("DEPENDS"),
											Direction.OUTGOING)) {
										Node c = r2.getEndNode();
										if (c.hasLabel(label("Class")) &&
												structClass[1].equals(c.getProperty("externalId"))) {
											break pgl;
										}
									}
									cpg = null;
								}
								if (cpg != null) {
									u.createRelationshipTo(cpg, withName("IN"));
								}
							}
						}
					}
				}
			}

			if (linkGroups != null) {
				for (String[] structGroup : linkGroups) {
					if (structGroup != null && structGroup[0] != null && structGroup[1] != null) {
						Structure s = this.structures.get(structGroup[0]);
						if (s != null) {
							Node structure = s.getNode();
							if (structure != null) {
								Node fg = null;
								for (Relationship r : structure.getRelationships(
										withName("DEPENDS"), Direction.INCOMING)) {
									fg = r.getStartNode();
									if (fg.hasLabel(label("FunctionalGroup")) &&
											structGroup[1].equals(fg.getProperty("externalId"))) {
										break;
									}
								}
								if (fg != null) {
									u.createRelationshipTo(fg, withName("IN"));
								}
							}
						}
					}
				}
			}
		}
	}

	public void createStudent(JsonObject object, String profileExternalId, String module, JsonArray fieldOfStudy,
			String[][] linkClasses, String[][] linkGroups, JsonArray relative) {
		final String error = studentValidator.validate(object);
		if (error != null) {
			log.warn(error);
		} else {
			Node u = getDb().createNode(label("User"));
			JsonUtil.jsonToNode(object, u);

			JsonArray structures = object.getArray("structures");
			if (structures != null && structures.size() > 0) {
				for (Object o : structures) {
					if (!(o instanceof String)) continue;
					Structure s = this.structures.get(o);
					if (s != null) {
						Node structure = s.getNode();
						Node spg = s.getProfileGroup(profileExternalId);
						if (structure != null) {
							u.createRelationshipTo(structure, withName("ADMINISTRATIVE_ATTACHMENT"));
						}
						if (spg != null) {
							u.createRelationshipTo(spg, withName("IN"));
						}
					}
				}
			}
			Set<Structure> structs = new HashSet<>();
			Set<Node> classes = new HashSet<>();
			if (linkClasses != null) {
				for (String[] structClass : linkClasses) {
					if (structClass != null && structClass[0] != null && structClass[1] != null) {
						Structure s = this.structures.get(structClass[0]);
						if (s != null) {
							structs.add(s);
							Node spg = s.getProfileGroup(profileExternalId);
							if (spg != null) {
								Node cpg = null;
								pgl: for (Relationship r : spg.getRelationships(withName("DEPENDS"),
										Direction.INCOMING)) {
									cpg = r.getStartNode();
									for (Relationship r2 : cpg.getRelationships(withName("DEPENDS"),
											Direction.OUTGOING)) {
										Node c = r2.getEndNode();
										if (c.hasLabel(label("Class")) &&
												structClass[1].equals(c.getProperty("externalId"))) {
											classes.add(c);
											break pgl;
										}
									}
									cpg = null;
								}
								if (cpg != null) {
									u.createRelationshipTo(cpg, withName("IN"));
								}
							}
						}
					}
				}
			}

			if (linkGroups != null) {
				for (String[] structGroup : linkGroups) {
					if (structGroup != null && structGroup[0] != null && structGroup[1] != null) {
						Structure s = this.structures.get(structGroup[0]);
						if (s != null) {
							structs.add(s);
							Node structure = s.getNode();
							if (structure != null) {
								Node fg = null;
								for (Relationship r : structure.getRelationships(
										withName("DEPENDS"), Direction.INCOMING)) {
									fg = r.getStartNode();
									if (fg.hasLabel(label("FunctionalGroup")) &&
											structGroup[1].equals(fg.getProperty("externalId"))) {
										break;
									}
								}
								if (fg != null) {
									u.createRelationshipTo(fg, withName("IN"));
								}
							}
						}
					}
				}
			}
//				if (externalId != null && module != null) {
//					String query =
//							"START u=node:node_auto_index(externalId={userExternalId}), " +
//							"m=node:node_auto_index(externalId={moduleStudent}) " +
//							"CREATE UNIQUE u-[:FOLLOW]->m";
//					JsonObject p = new JsonObject()
//							.putString("userExternalId", externalId)
//							.putString("moduleStudent", module);
//					transactionHelper.add(query, p);
//				}
//				if (externalId != null && fieldOfStudy != null && fieldOfStudy.size() > 0) {
//					for (Object o : fieldOfStudy) {
//						if (!(o instanceof String)) continue;
//						String query =
//								"START u=node:node_auto_index(externalId={userExternalId}), " +
//								"f=node:node_auto_index(externalId={fieldOfStudyStudent}) " +
//								"CREATE UNIQUE u-[:COURSE]->f";
//						JsonObject p = new JsonObject()
//								.putString("userExternalId", externalId)
//								.putString("fieldOfStudyStudent", (String) o);
//						transactionHelper.add(query, p);
//					}
//				}
			if (relative != null && relative.size() > 0) {
				for (Object o : relative) {
					if (!(o instanceof String)) continue;
					Node r = getDb().index().getNodeAutoIndexer()
							.getAutoIndex().get("externalId", o).getSingle();
					if (r == null) continue;
					u.createRelationshipTo(r, withName("RELATED"));
					for (Structure s: structs) {
						Node srg = s.getProfileGroup(DefaultProfiles.RELATIVE_PROFILE_EXTERNAL_ID);
						if (srg != null) {
							r.createRelationshipTo(srg, withName("IN"));
						}
					}
					for (Node c : classes) {
						String rcgn = c.getProperty("name") + "-" +
								DefaultProfiles.RELATIVE_PROFILE.getString("name");
						for (Relationship rc : c.getRelationships(withName("DEPENDS"),
								Direction.INCOMING)) {
							Node cpg = rc.getStartNode();
							if (cpg.hasLabel(label("ProfileGroup")) &&
									rcgn.equals(cpg.getProperty("name"))) {
								r.createRelationshipTo(cpg, withName("IN"));
								break;
							}
						}
					}
				}
			}
		}
	}

	public Structure getStructure(String externalId) {
		return structures.get(externalId);
	}

	public Profile getProfile(String externalId) {
		return profiles.get(externalId);
	}

}
