package fr.wseduc.aaf;

import org.vertx.java.core.json.JsonObject;

public class ModuleImportProcessing extends BaseImportProcessing {

	protected ModuleImportProcessing(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		parse(new StudentImportProcessing0(path));
	}

	@Override
	public String getMappingResource() {
		return "dictionary/mapping/aaf/MefEducNat.json";
	}

	@Override
	public void process(JsonObject object) {
		importer.createModule(object);
	}

	@Override
	protected String getFileRegex() {
		return  ".*?MefEducNat_[0-9]{4}\\.xml";
	}

}
