package com.global.api.utils;

import com.global.api.entities.enums.IStringConstant;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Element {
    private Document doc;
    private org.w3c.dom.Element element;

    public Element(Document doc, org.w3c.dom.Element element) {
        this.doc = doc;
        this.element = element;
    }

    public Element firstChild() {
        return Element.fromNode(doc, this.element.getFirstChild());
    }

    public Element remove(String tagName) {
        Element child = get(tagName);
        if(child != null) {
            element.removeChild(child.getElement());
        }
        return this;
    }

    public Element set(String name, String value){
        this.element.setAttribute(name, value);
        return this;
    }
    public Element set(String name, IStringConstant value) {
        return set(name, value.getValue());
    }

    public Element text(String text){
        if(text == null)
            text = "";
        this.element.appendChild(doc.createTextNode(text));
        return this;
    }
    public Element text(IStringConstant text) {
        return text(text.getValue());
    }

    public Element append(Element child) {
        this.doc.adoptNode(child.getElement());
        this.element.appendChild(child.getElement());
        return this;/**/
    }

    public String tag() {
        return this.element.getTagName();
    }

    public org.w3c.dom.Element getElement() { return this.element; }

    public static Element fromNode(Document doc, Node node) {
        return new Element(doc, (org.w3c.dom.Element)node);
    }

    public boolean has(String tagName) {
        return this.element.getElementsByTagName(tagName).getLength() > 0;
    }

    public Element get(String tagName) {
        return Element.fromNode(doc, this.element.getElementsByTagName(tagName).item(0));
    }

    public Element[] getAll() {
        NodeList nodes = this.element.getChildNodes();

        Element[] elements = new Element[nodes.getLength()];
        for(int i = 0; i < nodes.getLength(); i++)
            elements[i] = Element.fromNode(this.doc, nodes.item(i));

        return elements;
    }
    public Element[] getAll(String tagName) {
        NodeList nodes = this.element.getElementsByTagName(tagName);

        Element[] elements = new Element[nodes.getLength()];
        for(int i = 0; i < nodes.getLength(); i++)
            elements[i] = Element.fromNode(this.doc, nodes.item(i));

        return elements;
    }

    public String getAttributeString(String attributeName) {
        return this.element.getAttribute(attributeName);
    }

    public String getString(String... tagNames) {
        for(String tagName: tagNames) {
            org.w3c.dom.Element element = (org.w3c.dom.Element)this.element.getElementsByTagName(tagName).item(0);
            if(element != null) {
                return element.getTextContent();
            }
        }
        return null;
    }

    public Integer getInt(String tagName) {
        org.w3c.dom.Element element = (org.w3c.dom.Element)this.element.getElementsByTagName(tagName).item(0);
        if(element != null) {
            String value = element.getTextContent();
            if(StringUtils.isNullOrEmpty(value)) {
                return null;
            }
            return Integer.parseInt(element.getTextContent());
        } return null;
    }

    public BigDecimal getDecimal(String tagName) {
        org.w3c.dom.Element element = (org.w3c.dom.Element)this.element.getElementsByTagName(tagName).item(0);
        if(element != null) {
            return new BigDecimal(element.getTextContent());
        } return null;
    }

    public Date getDate(String... tagNames) {
        return getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"), tagNames);
    }
    public Date getDate(SimpleDateFormat formatter, String... tagNames) {
        for(String tagName: tagNames) {
            org.w3c.dom.Element element = (org.w3c.dom.Element)this.element.getElementsByTagName(tagName).item(0);
            if(element != null) {
                try {
                    return formatter.parse(element.getTextContent());
                }
                catch (ParseException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public DateTime getDateTime(String... tagNames) {
        return getDateTime(null, tagNames);
    }
    public DateTime getDateTime(DateTimeFormatter format, String... tagNames) {
        for(String tagName: tagNames) {
            org.w3c.dom.Element element = (org.w3c.dom.Element)this.element.getElementsByTagName(tagName).item(0);
            if(element != null) {
                String value = element.getTextContent();
                if(!StringUtils.isNullOrEmpty(value)) {
                    if (format == null) {
                        return DateTime.parse(value);
                    }
                    return DateTime.parse(value, format);
                }
                return null;
            }
        }
        return null;
    }
}