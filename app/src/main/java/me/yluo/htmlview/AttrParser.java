package me.yluo.htmlview;

import android.util.Log;

import java.util.Hashtable;
import java.util.Locale;

/**
 * html属性解析器
 * 对症下药
 * a - > href
 * img -> src
 * font -> color...
 */
public class AttrParser {

    private static final Hashtable<String, Integer> sColorMap;
    public static final int COLOR_NONE = 0;

    static {
        sColorMap = new Hashtable<>();
        sColorMap.put("aqua", 0xFF00FFFF);
        sColorMap.put("black", 0xFF000000);
        sColorMap.put("blue", 0xFF0000FF);
        sColorMap.put("darkgrey", 0xFFA9A9A9);
        sColorMap.put("fuchsia", 0xFFFF00FF);
        sColorMap.put("gray", 0xFF808080);
        sColorMap.put("grey", 0xFF808080);
        sColorMap.put("green", 0xFF008000);
        sColorMap.put("lightblue", 0xFFADD8E6);
        sColorMap.put("lightgrey", 0xFFD3D3D3);
        sColorMap.put("lime", 0xFF00FF00);
        sColorMap.put("maroon", 0xFF800000);
        sColorMap.put("navy", 0xFF000080);
        sColorMap.put("olive", 0xFF808000);
        sColorMap.put("orange", 0xFFFFA500);
        sColorMap.put("purple", 0xFF800080);
        sColorMap.put("red", 0xFFFF0000);
        sColorMap.put("silver", 0xFFC0C0C0);
        sColorMap.put("teal", 0xFF008080);
        sColorMap.put("white", 0xFFFFFFFF);
        sColorMap.put("yellow", 0xFFFFFF00);
    }

    public static HtmlNode.HtmlAttr parserAttr(int type, char[] buf, int len) {
        HtmlNode.HtmlAttr attr = new HtmlNode.HtmlAttr();
        String attrStr = new String(buf, 0, len);
        switch (type) {
            case HtmlTag.A:
                attr.href = getAttrs(attrStr, 0, "href");
                break;
            case HtmlTag.IMG:
                attr.src = getAttrs(attrStr, 0, "src");
                break;
            case HtmlTag.FONT:
            case HtmlTag.P:
                attr.color = getTextColor(attrStr, 0);
                break;
        }
        return attr;
    }

    //只有块状标签才有意义
    //left right center
    //css
    private static int getTextAlign(int i, String s) {
        i = getValidStrPos(s, i, "text-align", 15);
        if (i > 0) {
            while (i < s.length() && (s.charAt(i) < 'a' || s.charAt(i) > 'z')) {
                i++;
            }

            if (s.startsWith("right", i)) {
                return 2;
            } else if (s.startsWith("center", i)) {
                return 1;
            } else if (s.startsWith("left", i)) {
                return 0;
            }
        }
        return -1;
    }

    //color="red" " color:red "
    //attr css
    private static int getTextColor(String s, int start) {
        int j = getValidStrPos(s, start, "color", 10);
        Log.d("====", s + "|" + start + "|" + j);
        if (j < 0) return COLOR_NONE;
        //color 排除background-color bgcolor
        if (j > start + 5 && ((s.charAt(j - 6) == '-') || (s.charAt(j - 6) == 'g'))) {
            return COLOR_NONE;
        }

        while (j < s.length() - 3) {
            if (s.charAt(j) == '=') {
                while (j < (s.length() - 3) && s.charAt(j) != '\"') {
                    j++;
                }

                if (s.charAt(j) == '\"') {
                    start = j + 1;
                    while (start < s.length()
                            && s.charAt(start) != '\"'
                            && s.charAt(start) != ' '
                            && s.charAt(start) != '\n') {
                        start++;
                    }

                    return getHtmlColor(j + 1, start, s);
                }

                return -1;
            } else if (s.charAt(j) == ':') {
                j++;
                while (j < s.length() - 3 && (s.charAt(j) == ' ' || s.charAt(j) == '\n')) {
                    j++;
                }

                start = j + 1;
                while (start < s.length()
                        && s.charAt(start) != ';'
                        && s.charAt(start) != ' '
                        && s.charAt(start) != '\n'
                        && s.charAt(start) != '\"') {
                    start++;
                }

                return getHtmlColor(j, start, s);
            } else {
                if (s.charAt(j) == '\"') {
                    return COLOR_NONE;
                }
                j++;
            }
        }

        return COLOR_NONE;
    }

    //text-decoration:none underline overline line-through
    //css
    //TextPaint tp = new TextPaint();
    //tp.setUnderlineText(true);  //1
    //tp.setStrikeThruText(true); //2
    //none //0
    public static int getTextDecoration(int start, String s) {
        int j = getValidStrPos(s, start, "text-decoration", 20);
        if (j < 0) return -1;

        while (j < s.length() && (s.charAt(j) < 'a' || s.charAt(j) > 'z')) {
            j++;
        }

        if (s.startsWith("underline", j)) {
            return 1;
        } else if (s.startsWith("line-through", j)) {
            return 2;
        } else if (s.startsWith("none", j)) {
            return 0;
        }

        return -1;
    }


    //a="b" src="" href=""
    private static String getAttrs(String source, int start, String to) {
        if (source.length() - start - 5 < to.length()) return null;
        int j = getValidStrPos(source, start, to, to.length() + 4);
        if (j < 0) return null;
        while (j < source.length() - 3) {
            if (source.charAt(j) == '=') {
                while (j < (source.length() - 3) && source.charAt(j) != '\"') {
                    j++;
                }
                if (source.charAt(j) == '\"') {
                    j++;
                    while (j < (source.length() - 2)
                            && (source.charAt(j) == ' '
                            || source.charAt(j) == '\n')) {
                        j++;
                    }
                    int i = j;
                    while (i < source.length() - 2
                            && source.charAt(i) != '\"'
                            && source.charAt(i) != ' '
                            && source.charAt(i) != '\n') {
                        i++;
                    }
                    if (i - j >= 0 && i < source.length()) {
                        return source.substring(j, i);
                    } else {
                        return source.substring(j, source.length());
                    }
                }
                return null;
            }
            j++;
        }
        return null;
    }


    //html color-> android color
    private static int getHtmlColor(int start, int end, String color) {
        if (end - start < 3) return COLOR_NONE;
        if (color.charAt(start) == '#') {
            if (end - start == 9) start += 2;
            if (end - start == 7) {
                int colorInt = Integer.parseInt(color.substring(start + 1, end), 16);
                return (colorInt | 0xff000000);
            }
            return COLOR_NONE;
        } else {
            Integer i = sColorMap.get(color.substring(start, end).toLowerCase(Locale.US));
            if (i != null) {
                return i;
            }
            return COLOR_NONE;
        }
    }

    //从source中找到to并且指定最小有效长度,如寻找style那么最小有效长度为17
    //style="color:red" 17
    //text-align:left 15
    private static int getValidStrPos(String source, int start, String to, int minlen) {
        int len = source.length() - start;
        if (len < minlen) return -1;

        int pos1 = 0;
        int pos2 = 0;
        while (pos1 <= len - minlen) {
            pos2 = 0;
            while (source.charAt(pos1) == to.charAt(pos2)) {
                pos2++;
                pos1++;
                if (pos2 == to.length()) {
                    return pos1;
                }
            }
            pos1++;
        }
        return -1;
    }
}
