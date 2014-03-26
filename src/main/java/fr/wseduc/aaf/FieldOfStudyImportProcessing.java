package fr.wseduc.aaf;

import org.vertx.java.core.json.JsonObject;

public class FieldOfStudyImportProcessing extends BaseImportProcessing {

	protected FieldOfStudyImportProcessing(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		parse(new ModuleImportProcessing(path));
	}

	@Override
	public String getMappingResource() {
		return "dictionary/mapping/aaf/MatEducNat.json";
	}

	@Override
	public void process(JsonObject object) {
		importer.createFieldOfStudy(object);
	}

	@Override
	protected String getFileRegex() {
		return ".*?MatiereEducNat_[0-9]{4}\\.xml";
	}

}
