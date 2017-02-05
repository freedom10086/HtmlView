package me.yluo.htmlview;


public class Attribute {
    private String src;//attr
    private String href;//attr
    private int color;//css,attr color
    private int bgColor;//css background-color or background{#xxxxxxx}

    //css text-align{left center right}
    private int textAlign;
    private int textDecoration;

    //not support
    //font-family: sans-serif;
    //private int font_family

    //css font-size{16px 1em}
    //attr size{1,2,3,4,5}
    private int font_size;

}
