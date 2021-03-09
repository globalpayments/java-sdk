package com.global.api.utils;

import com.global.api.entities.enums.IFlag;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.exceptions.ApiException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;

public class ElementTree {
    private Document doc;
    private HashMap<String, String> namespaces;

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    public ElementTree() {
        init(new HashMap<String, String>());
    }
    public ElementTree(HashMap<String, String> namespaces) {
        init(namespaces); 
    }

    public Element element(String tagName) {
        org.w3c.dom.Element element;

        if (tagName.contains(":")) {
            String[] data = tagName.split(":");
            String namespaceURI = namespaces.get(data[0]);
            element = doc.createElementNS(namespaceURI, tagName);
        }
        else {
            element = doc.createElement(tagName);
        }
        return new Element(doc, element, namespaces);
    }

    public Element subElement(Element parent, String tagName) {
        org.w3c.dom.Element child;

        if (tagName.contains(":")) {
            String[] data = tagName.split(":");
            String namespaceURI = namespaces.get(data[0]);
            child = doc.createElementNS(namespaceURI, tagName);
        }
        else {
            child = doc.createElement(tagName);
        }

        parent.getElement().appendChild(child);
        return new Element(doc, child, namespaces);
    }
    public Element subElement(Element parent, String tagName, String value) {
        if(value == null || value.equals(""))
            return null;
        return subElement(parent, tagName).text(value);
    }
    public Element subElement(Element parent, String tagName, int value) {
        if(value == 0)
            return null;
        return subElement(parent, tagName).text(value + "");
    }
    public Element subElement(Element parent, String tagName, Integer value) {
        if(value == null)
            return null;
        return subElement(parent, tagName).text(value + "");
    }
    public Element subElement(Element parent, String tagName, BigDecimal value) {
        if(value == null)
            return null;
        return subElement(parent, tagName, value.toString());
    }
    public Element subElement(Element parent, String tagName, IStringConstant value) {
        if(value == null)
            return null;
        return subElement(parent, tagName, value.getValue());
    }
    public Element subElement(Element parent, String tagName, IMappedConstant value) {
        if(value == null) {
            return null;
        }
        return subElement(parent, tagName, value.getValue(null));
    }
    public Element subElement(Element parent, String tagName, IFlag value) {
        if(value == null)
            return null;
        return subElement(parent, tagName, value.toString());
    }

    public void addNamespace(String prefix, String uri) {
        this.namespaces.put(prefix, uri);
    }

    public String toString(Element root) {
        doc.appendChild(root.getElement());

        try {
            TransformerFactory tFact = TransformerFactory.newInstance();
            Transformer trans = tFact.newTransformer();

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            return writer.toString();
        } catch (TransformerException e) {
            return e.getMessage();
        } finally {
            doc.removeChild(root.getElement());
        }
    }

    public Element get(String tagName) {
        Node node;

        if (tagName.contains(":")) {
            String[] data = tagName.split(":");
            String namespaceURI = namespaces.get(data[0]);
            node = doc.getElementsByTagNameNS(namespaceURI, tagName).item(0);
        }
        else {
            node = doc.getElementsByTagName(tagName).item(0);
        }

        if(node != null) {
            return Element.fromNode(doc, node, namespaces);
        } return null;
    }

    public static ElementTree parse(byte[] buffer) throws ApiException {
        String xmlString = "";
        for(byte b: buffer) {
            xmlString += (char)b;
        }
        return parse(xmlString);
    }

    public static ElementTree parse(String xml) throws ApiException {
        return parse(xml, new HashMap<String, String>());
    }

    public static ElementTree parse(String xml, HashMap<String, String> namespaces) throws ApiException{
        try {
            InputSource is = new InputSource(new StringReader(xml));

            ElementTree rvalue = new ElementTree(namespaces);
            rvalue.setDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is));
            return rvalue;
        } catch(ParserConfigurationException e) {
            throw new ApiException(e.getMessage());
        } catch(SAXException e) {
            throw new ApiException(e.getMessage());
        } catch(IOException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private void init(HashMap<String, String> namespaces) {
        try {
            this.namespaces = namespaces;
            this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }
}