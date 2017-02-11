package me.yluo.htmlview;

public interface ParserCallback {

    void startDocument(int initLen);

    void startElement(HtmlNode node);

    void characters(char[] ch, int start, int len);

    void endElement(int type, String name);

    void endDocument();
}
