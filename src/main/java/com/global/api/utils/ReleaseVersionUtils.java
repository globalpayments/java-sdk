package com.global.api.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Utility for retrieving the SDK release version from the project POM.
 * When running under test conditions (system property {@code sdk.testing}=true),
 * an empty string is returned to avoid file-system lookups.
 */
public class ReleaseVersionUtils {

    /**
     * Returns the SDK release version extracted from {@code pom.xml}.
     * If the {@code sdk.testing} system property is set to {@code true}, an empty string is returned.
     *
     * @return the release version, or an empty string when testing or if the version cannot be read
     */
    public static String getReleaseVersion() {
        if ("true".equalsIgnoreCase(System.getProperty("sdk.testing"))) {
            return "";
        }

        String version = "";
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);
            Document pomXml = dbf.newDocumentBuilder().parse(new File("pom.xml"));
            Element pomRoot = (Element) pomXml.getElementsByTagName("project").item(0);
            version = pomRoot.getElementsByTagName("version").item(0).getTextContent();
        } catch (Exception ex) {
            System.out.println("JAVA SDK version could not be extracted from pom.xml file.");
        }
        return version;
    }
}
