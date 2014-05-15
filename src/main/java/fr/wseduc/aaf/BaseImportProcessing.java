package fr.wseduc.aaf;

import fr.wseduc.aaf.dictionary.Importer;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
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
				xr.setEntityResolver(new EntityResolver2() {
					@Override
					public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
						return null;
					}

					@Override
					public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
						return resolveEntity(publicId, systemId);
					}

					@Override
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						if (systemId.equals("ficAlimMENESR.dtd")) {
							Reader reader = new FileReader(path + File.separator + "ficAlimMENESR.dtd");
							return new InputSource(reader);
						} else {
							return null;
						}
					}
				});
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
