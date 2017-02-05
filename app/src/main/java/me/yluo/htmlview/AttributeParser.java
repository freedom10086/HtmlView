package me.yluo.htmlview;

import java.util.Hashtable;
import java.util.Locale;

public class AttributeParser {

    private static final Hashtable<String, Integer> sColorMap;
    private static final int COLOR_NONE = -1;

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

    //只有块状标签才有意义
    //left right center
    //css
    private static int getTextAlign(int i, String s) {
        i = getValidStrPos(s, i, "text-align", 15);
        if (i > 0) {
            while (i < s.length() && (s.charAt(i) < 'a' || s.charAt(i) > 'z')) {
                i++;
            }

            if (s.indexOf("right", i) >= 0) {
                return 2;
            } else if (s.indexOf("center", i) >= 0) {
                return 1;
            } else if (s.indexOf("left", i) >= 0) {
                return 0;
            }
        }
        return -1;
    }

    //color="red" " color:red "
    //attr css
    private static int getTextColor(int i, String s) {
        int j = getValidStrPos(s, i, "color", 10);
        if (j < 0) return -1;
        //color 排除background-color bgcolor
        if (j > i + 5
                && (s.charAt(j - 6) == '-')
                || (s.charAt(j - 6) == 'g')) {
            return -1;
        }

        while (j < s.length() - 3) {
            if (s.charAt(j) == '=') {
                while (j < (s.length() - 3) && s.charAt(j) != '\"') {
                    j++;
                }

                if (s.charAt(j) == '\"') {
                    i = j + 1;
                    while (i < s.length()
                            && s.charAt(i) != '\"'
                            && s.charAt(i) != ' '
                            && s.charAt(i) != '\n') {
                        i++;
                    }

                    return getHtmlColor(j + 1, i, s);
                }

                return -1;
            } else if (s.charAt(j) == ':') {
                j++;
                while (j < s.length() - 3 && (s.charAt(j) == ' ' || s.charAt(j) == '\n')) {
                    j++;
                }

                i = j + 1;
                while (i < s.length()
                        && s.charAt(i) != ';'
                        && s.charAt(i) != ' '
                        && s.charAt(i) != '\n'
                        && s.charAt(i) != '"') {
                    i++;
                }

                return getHtmlColor(j, i, s);
            } else {
                if (s.charAt(j) == '\"') {
                    return -1;
                }
                j++;
            }
        }

        return -1;
    }

    //text-decoration:none underline overline line-through
    //css
    //TextPaint tp = new TextPaint();
    //tp.setUnderlineText(true);
    //tp.setStrikeThruText(true);
    private static int getTextDecoration(int i, String s) {
        int j = getValidStrPos(s, i, "text-decoration", 20);
        if (j < 0) return -1;

        while (j < s.length() - 4) {
            if (s.charAt(j) == '=') {
                while (j < (s.length() - 3) && s.charAt(j) != '\"') {
                    j++;
                }

                if (s.charAt(j) == '\"') {
                    i = j + 1;
                    while (i < s.length()
                            && s.charAt(i) != '\"'
                            && s.charAt(i) != ' '
                            && s.charAt(i) != '\n') {
                        i++;
                    }

                    return getHtmlColor(j + 1, i, s);
                }

                return -1;
            } else if (s.charAt(j) == ':') {
                j++;
                while (j < s.length() - 3 && (s.charAt(j) == ' ' || s.charAt(j) == '\n')) {
                    j++;
                }

                i = j + 1;
                while (i < s.length()
                        && s.charAt(i) != ';'
                        && s.charAt(i) != ' '
                        && s.charAt(i) != '\n'
                        && s.charAt(i) != '"') {
                    i++;
                }

                return getHtmlColor(j, i, s);
            } else {
                if (s.charAt(j) == '\"') {
                    return -1;
                }
                j++;
            }
        }

        return -1;
    }


    //a="b"
    private String getAttrs(String source, int start, String to) {
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
                            || source.charAt(j) == '\n')){
                        j++;
                    }
                    int i = j;
                    while (i < source.length()-2
                            && source.charAt(i) != '\"'
                            && source.charAt(i) != ' '
                            && source.charAt(i) != '\n') {
                        i++;
                    }
                    if (i - j > 0) {
                        return source.substring(j, i);
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
        if (end - start < 3) return -1;
        if (color.charAt(start) == '#') {
            if (end - start == 9) start += 2;
            if (end - start == 7) {
                int colorInt = Integer.parseInt(color.substring(start + 1, end), 16);
                return (colorInt | 0xff000000);
            }
            return -1;
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
