package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.vertx.java.core.json.JsonObject;

public class UserImportProcessing extends BaseImportProcessing {

	private final Importer importer = Importer.getInstance();

	protected UserImportProcessing(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		parse(new PersonnelImportProcessing(path));
	}

	@Override
	public String getMappingResource() {
		return "dictionary/mapping/aaf/PersRelEleve.json";
	}

	@Override
	public void process(JsonObject object) {
		importer.createUser(object);
	}

	@Override
	protected String getFileRegex() {
		return ".*?PersRelEleve_[0-9]{4}\\.xml";
	}

}
