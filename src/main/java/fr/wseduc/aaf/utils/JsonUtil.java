package fr.wseduc.aaf.utils;

import org.neo4j.graphdb.Node;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Scanner;

public final class JsonUtil {

	private JsonUtil() {}

	public static JsonObject loadFromResource(String resource) {
		String src = new Scanner(JsonUtil.class.getClassLoader()
				.getResourceAsStream(resource), "UTF-8")
				.useDelimiter("\\A").next();
		return new JsonObject(src);
	}

	public static Object convert(String value, String type) {
		if (type == null) {
			return value;
		}
		Object res;
		try {
			switch (type.replaceAll("array-", "")) {
				case "boolean" :
					String v = value.toLowerCase().replaceFirst("(y|o)", "true").replaceFirst("n", "false");
					res = Boolean.parseBoolean(v);
					break;
				case "int" :
					res = Integer.parseInt(value);
					break;
				case "long" :
					res = Long.parseLong(value);
					break;
				default :
					res = value;
			}
		} catch (RuntimeException e) {
			res = value;
		}
		return res;
	}

	public static void jsonToNode(JsonObject j, Node n) {
		if (j == null || n == null) return;
		for (String attr : j.getFieldNames()) {
			Object v = j.getValue(attr);
			if (v instanceof JsonArray) {
				JsonArray ja = (JsonArray) v;
				String [] strings = new String[ja.size()];
				for (int i = 0; i < strings.length; i++) {
					strings[i] = ja.get(i);
				}
				v = strings;
			}
			n.setProperty(attr, v);
		}
	}

}
