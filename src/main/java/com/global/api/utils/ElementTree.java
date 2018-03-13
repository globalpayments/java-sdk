package com.global.api.utils;

import com.global.api.entities.enums.IFlag;
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

public class ElementTree {
    private Document doc;

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    public ElementTree(){
        try {
            this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }

    public Element element(String tagName) {
        org.w3c.dom.Element element = doc.createElement(tagName);
        return new Element(doc, element);
    }

    public Element subElement(Element parent, String tagName) {
        org.w3c.dom.Element child = doc.createElement(tagName);
        parent.getElement().appendChild(child);
        return new Element(doc, child);
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
    public Element subElement(Element parent, String tagName, IFlag value) {
        if(value == null)
            return null;
        return subElement(parent, tagName, value.toString());
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
        Node node = doc.getElementsByTagName(tagName).item(0);
        if(node != null) {
            return Element.fromNode(doc, node);
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
        try {
            InputSource is = new InputSource(new StringReader(xml));

            ElementTree rvalue = new ElementTree();
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
}