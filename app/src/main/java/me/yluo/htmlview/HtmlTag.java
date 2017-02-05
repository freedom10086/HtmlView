package me.yluo.htmlview;


public class HtmlTag {

    public static final int UNKNOWN = -1;

    /**
     * 内联标签
     */
    public static final int FONT = 1;//不赞同字体color face size
    public static final int TT = 2;//等宽的文本效果
    public static final int I = 3;//斜体
    public static final int U = 4;//下划线
    public static final int BIG = 5;
    public static final int SMALL = 6;
    public static final int EM = 7;//强调的内容
    public static final int STRONG = 8;//语气更强的强调
    public static final int B = 9;//加粗
    public static final int CODE = 10;//代码
    public static final int KBD = 11;//定义键盘文本
    public static final int Q = 14;//引用
    public static final int MARK = 15;//突出显示部分文本
    public static final int A = 16; //href
    public static final int IMG = 17; //src
    public static final int BR = 18;
    public static final int SUB = 19;
    public static final int SUP = 20;
    public static final int INS = 21;//下划线
    public static final int DEL = 22;//删除线
    public static final int S = 23;//不赞同删除线
    public static final int STRIKE = 24;//不赞同删除线DEL替代
    public static final int SPAN = 25;

    /**
     * 块标签
     */
    public static final int HEADER = 50;
    public static final int FOOTER = 51;
    public static final int DIV = 53;

    public static final int P = 54;
    public static final int UL = 55;
    public static final int OL = 56;
    public static final int LI = 57;

    public static final int H1 = 61;
    public static final int H2 = 62;
    public static final int H3 = 63;
    public static final int H4 = 64;
    public static final int H5 = 65;
    public static final int H6 = 66;

    public static final int PRE = 70;
    public static final int BLOCKQUOTE = 71;
    public static final int HR = 72;

    public static final int TABLE = 81;
    public static final int CAPTION = 82;
    public static final int THEAD = 83;
    public static final int TFOOT = 84;
    public static final int TBODY = 85;
    public static final int TR = 86;
    public static final int TH = 87;
    public static final int TD = 88;

    public static final int VEDIO = 91; //src
    public static final int AUDIO = 92; //src

    public static boolean isBolckTag(int i) {
        return i >= 50;
    }
}
