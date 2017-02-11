package me.yluo.htmlview;


public class HtmlNode {
    public int type = HtmlTag.UNKNOWN;
    public String name;
    public int start = -1;
    public HtmlAttr attr;


    public HtmlNode(int type, String name, HtmlAttr attr) {
        this.type = type;
        this.name = name;
        this.attr = attr;
    }

    public static class HtmlAttr {
        String src;//attr
        String href;//attr
        int color = -1;//css,attr color
        int textAlign;
        int textDecoration;
    }
}
