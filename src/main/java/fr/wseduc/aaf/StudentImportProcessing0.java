package fr.wseduc.aaf;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class StudentImportProcessing0 extends StudentImportProcessing {

	protected final Set<String> resp = new HashSet<>();

	protected StudentImportProcessing0(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		parse(new UserImportProcessing(path, resp));
	}

	@Override
	public void process(JsonObject object) {
		JsonArray r = parseRelativeField(object.getArray("relative"));
		if (r != null) {
			resp.addAll(r.toList());
		}
	}

}
