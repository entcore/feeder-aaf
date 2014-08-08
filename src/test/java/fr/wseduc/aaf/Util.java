package fr.wseduc.aaf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Util {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static String toCypherUri(String baseUri) {
		try {
			return new URL(new URL(baseUri), "/db/data/cypher").toString();
		} catch (Exception e) {
			throw new RuntimeException("Error constructing cypher uri from " + baseUri, e);
		}
	}

	@SuppressWarnings("unchecked")
	static Map createPostData(String statement, Map<String, Object> params) {
		Map postData = new HashMap();
		postData.put("query", statement);
		postData.put("params", params == null ? Collections.EMPTY_MAP : params);
		return postData;
	}

	static String toJson(Map postData) {
		try {
			return OBJECT_MAPPER.writeValueAsString(postData);
		} catch (IOException e) {
			throw new RuntimeException("Can't convert " + postData + " to json", e);
		}
	}

}
