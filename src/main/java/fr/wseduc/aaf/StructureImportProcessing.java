package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.DefaultFunctions;
import fr.wseduc.aaf.dictionary.Structure;
import org.neo4j.graphdb.Transaction;
import org.vertx.java.core.json.JsonObject;

import static fr.wseduc.aaf.dictionary.DefaultProfiles.*;

public class StructureImportProcessing extends BaseImportProcessing {

	protected StructureImportProcessing(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		// create profiles
		Transaction tx = importer.getDb().beginTx();
		try {
			importer.createProfile(STUDENT_PROFILE);
			importer.createProfile(RELATIVE_PROFILE);
			importer.createProfile(PERSONNEL_PROFILE);
			importer.createProfile(TEACHER_PROFILE);
			importer.createProfile(GUEST_PROFILE);
			DefaultFunctions.createOrUpdateFunctions(importer);
			tx.success();
		} catch (Exception e) {
			tx.failure();
			throw e;
		} finally {
			tx.close();
		}
		// parse etab file
		parse(new FieldOfStudyImportProcessing(path));
	}

	@Override
	public String getMappingResource() {
		return "dictionary/mapping/aaf/EtabEducNat.json";
	}

	@Override
	public void process(JsonObject object) {
		Structure structure = importer.createStructure(object);
		if (structure != null) {
			structure.addAttachment();
		}
	}

	@Override
	protected String getFileRegex() {
		return ".*?EtabEducNat_[0-9]{4}\\.xml";
	}

}
