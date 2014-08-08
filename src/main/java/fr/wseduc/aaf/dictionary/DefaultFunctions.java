package fr.wseduc.aaf.dictionary;

import org.vertx.java.core.json.JsonObject;


public final class DefaultFunctions {

	private DefaultFunctions() {}

	public static final String ADMIN_LOCAL_EXTERNAL_ID = "ADMIN_LOCAL";
	public static final JsonObject ADMIN_LOCAL = new JsonObject()
			.putString("externalId", ADMIN_LOCAL_EXTERNAL_ID)
			.putString("name", "AdminLocal");

	public static final String CLASS_ADMIN_EXTERNAL_ID = "CLASS_ADMIN";
	public static final JsonObject CLASS_ADMIN = new JsonObject()
			.putString("externalId", CLASS_ADMIN_EXTERNAL_ID)
			.putString("name", "ClassAdmin");

	public static void createOrUpdateFunctions(Importer importer) {
		Profile p = importer.getProfile(DefaultProfiles.PERSONNEL_PROFILE_EXTERNAL_ID);
		if (p != null) {
			JsonObject f = DefaultFunctions.ADMIN_LOCAL;
			p.createFunctionIfAbsent(f.getString("externalId"), f.getString("name"));
		}
		Profile t = importer.getProfile(DefaultProfiles.PERSONNEL_PROFILE_EXTERNAL_ID);
		if (t != null) {
			JsonObject f = DefaultFunctions.CLASS_ADMIN;
			t.createFunctionIfAbsent(f.getString("externalId"), f.getString("name"));
		}
	}

}
