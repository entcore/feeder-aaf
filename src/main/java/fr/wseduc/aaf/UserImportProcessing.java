package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.vertx.java.core.json.JsonObject;

import java.util.Set;

public class UserImportProcessing extends BaseImportProcessing {

	private final Importer importer = Importer.getInstance();
	protected final Set<String> resp;

	protected UserImportProcessing(String path, Set<String> resp) {
		super(path);
		this.resp = resp;
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
		if (resp.contains(object.getString("externalId"))) {
			importer.createUser(object);
		}
	}

	@Override
	protected String getFileRegex() {
		return ".*?PersRelEleve_[0-9]{4}\\.xml";
	}

}
