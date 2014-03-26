package fr.wseduc.aaf;

import org.vertx.java.core.json.JsonObject;

public interface ImportProcessing {

	void start() throws Exception;

	String getMappingResource();

	void process(JsonObject object);

}
