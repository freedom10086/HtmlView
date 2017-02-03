package me.yluo.htmlview;

import org.xml.sax.Attributes;

public interface ContentHandler {

    void startDocument();

    void endDocument();

    void startElement(String name, Attributes atts);

    void endElement(String name);

    void characters(char[] ch, int start, int len);
}
