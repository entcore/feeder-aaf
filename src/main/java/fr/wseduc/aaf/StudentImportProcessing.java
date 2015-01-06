package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.DefaultProfiles;
import fr.wseduc.aaf.dictionary.Structure;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class StudentImportProcessing extends BaseImportProcessing {

	protected StudentImportProcessing(String path) {
		super(path);
	}

	@Override
	public void start() throws Exception {
		parse(null);
	}

	@Override
	public String getMappingResource() {
		return "dictionary/mapping/aaf/Eleve.json";
	}

	@Override
	public void process(JsonObject object) {
		String[][] classes = createClasses(object.getArray("classes"));
		String[][] groups = createGroups(object.getArray("groups"));
		JsonArray relative = parseRelativeField(object.getArray("relative"));
		importer.createStudent(object, DefaultProfiles.STUDENT_PROFILE_EXTERNAL_ID,
				null, null, classes, groups, relative);
	}

	protected JsonArray parseRelativeField(JsonArray relative) {
		JsonArray res = null;
		if (relative != null && relative.size() > 0) {
			res = new JsonArray();
			for (Object o : relative) {
				if (!(o instanceof String)) continue;
				String [] r = ((String) o).split("\\$");
				if (r.length == 6 && !"0".equals(r[3])) {
					res.add(r[0]);
				}
			}
		}
		return res;
	}

	protected String[][] createGroups(JsonArray groups) {
		String [][] linkStructureGroups = null;
		if (groups != null && groups.size() > 0) {
			linkStructureGroups = new String[groups.size()][2];
			int i = 0;
			for (Object o : groups) {
				if (!(o instanceof String)) continue;
				String [] g = ((String) o).split("\\$");
				if (g.length == 2) {
					Structure s = importer.getStructure(g[0]);
					if (s != null) {
						s.createFunctionalGroupIfAbsent((String) o, g[1]);
						linkStructureGroups[i][0] = g[0];
						linkStructureGroups[i++][1] = (String) o;
					}
				}
			}
		}
		return linkStructureGroups;
	}

	protected String[][] createClasses(JsonArray classes) {
		String [][] linkStructureClasses = null;
		if (classes != null && classes.size() > 0) {
			linkStructureClasses = new String[classes.size()][2];
			int i = 0;
			for (Object o : classes) {
				if (!(o instanceof String)) continue;
				String [] c = ((String) o).split("\\$");
				if (c.length == 2) {
					Structure s = importer.getStructure(c[0]);
					if (s != null) {
						s.createClassIfAbsent((String) o, c[1]);
						linkStructureClasses[i][0] = c[0];
						linkStructureClasses[i++][1] = (String) o;
					}
				}
			}
		}
		return linkStructureClasses;
	}

	@Override
	protected String getFileRegex() {
		return ".*?_Eleve_[0-9]{4}\\.xml";
	}

}
