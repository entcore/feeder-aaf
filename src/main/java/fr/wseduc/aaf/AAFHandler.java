package fr.wseduc.aaf;

import fr.wseduc.aaf.utils.JsonUtil;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AAFHandler extends DefaultHandler {

	private String currentTag = "";
	private String currentAttribute = "";
	private JsonObject currentStructure;
	private final JsonObject mapping;
	private final ImportProcessing processing;
	private static final Pattern frenchDatePatter = Pattern.compile("^([0-9]{2})/([0-9]{2})/([0-9]{4})$");

	public AAFHandler(ImportProcessing processing) {
		this.processing = processing;
		this.mapping = JsonUtil.loadFromResource(processing.getMappingResource());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentTag = localName;
		switch (localName) {
			case "addRequest" :
				currentStructure = new JsonObject();
				break;
			case "attr" :
				currentAttribute = attributes.getValue(0);
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentTag = "";
		switch (localName) {
			case "addRequest" :
				processing.process(currentStructure);
				break;
			case "attr" :
				currentAttribute = "";
				break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		final String s = new String(ch, start, length);
		switch (currentTag) {
			case "id" : addExternalId(s);
				break;
			case "value" : addValueInAttribute(s);
				break;
		}
	}

	private void addValueInAttribute(String s) throws SAXException {
		JsonObject j = mapping.getObject(currentAttribute);
		if (j == null) {
			throw new SAXException("Unknown attribute " + currentAttribute);
		}
		if (currentStructure == null) {
			throw new SAXException("Value is found but structure isn't defined.");
		}
		String type = j.getString("type");
		String attribute = j.getString("attribute");
		if ("birthDate".equals(attribute)) {
			s = convertDate(s);
		}
		if (type != null && type.contains("array")) {
			JsonArray a = currentStructure.getArray(attribute);
			if (a == null) {
				a = new JsonArray();
				currentStructure.putArray(attribute, a);
			}
			a.add(JsonUtil.convert(s, type));
		} else {
			currentStructure.putValue(attribute, JsonUtil.convert(s, type));
		}
	}

	private String convertDate(String s) {
		Matcher m = frenchDatePatter.matcher(s);
		if (m.find()) {
			return m.group(3) + "-" + m.group(2) + "-" + m.group(1);
		}
		return s;
	}

	private void addExternalId(String s) throws SAXException {
		if (currentStructure != null) {
			currentStructure.putString("externalId", s);
		} else {
			throw new SAXException("Id is found but structure isn't defined.");
		}
	}

}
