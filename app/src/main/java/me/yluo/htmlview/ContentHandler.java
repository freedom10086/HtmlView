package me.yluo.htmlview;

public interface ContentHandler {

    void startDocument(int initLen);

    void startElement(String name, HtmlNode node);

    void endElement(int type, String name);

    void characters(char[] ch, int start, int len);

    void endDocument();
}
