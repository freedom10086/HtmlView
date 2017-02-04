package me.yluo.htmlview;

public interface ContentHandler {

    void startDocument();

    void endDocument();

    void startElement(int type, String name, String atts);

    void endElement(int type, String name);

    void characters(char[] ch, int start, int len);
}
