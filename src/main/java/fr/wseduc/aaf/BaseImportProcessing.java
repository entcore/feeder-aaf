package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public abstract class BaseImportProcessing implements ImportProcessing {

	protected static final Logger log = LoggerFactory.getLogger(BaseImportProcessing.class);
	protected final Importer importer = Importer.getInstance();
	protected final String path;

	protected BaseImportProcessing(String path) {
		this.path = path;
	}

	protected void parse(final ImportProcessing importProcessing) throws Exception {
		Transaction tx = importer.getDb().beginTx();
		try {
			File [] files = new File(path).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(getFileRegex());
				}
			});
			Arrays.sort(files);
			for (File file : files) {
				log.info("Parsing file : " + file.getName());
				InputSource in = new InputSource(new FileInputStream(file));
				AAFHandler sh = new AAFHandler(this);
				XMLReader xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(sh);
				xr.parse(in);
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			tx.close();
		}
		if (importProcessing != null) {
			importProcessing.start();
		}
	}

	protected abstract String getFileRegex();

}
