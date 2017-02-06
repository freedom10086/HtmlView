package me.yluo.htmlview;


public class HtmlNode {
    public int type = HtmlTag.UNKNOWN;
    public int start = -1;



    private String src;//attr
    private String href;//attr
    private int color;//css,attr color
    private int textAlign;
    private int textDecoration;

    //not support
    //font-family: sans-serif;
    //private int font_family

    //css font-size{16px 1em}
    //attr size{1,2,3,4,5}
    //private int font_size;


    public HtmlNode(int type) {
        this.type = type;
    }
}
