package me.yluo.htmlview;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;

public class HtmlParser {
    private static final char EOF = (char) -1;
    private static final Hashtable replaceMap = new Hashtable();
    private static final int MAX_TAG_LEN = 16;
    private static final int MAX_ATTR_LEN = 256;

    static {
        replaceMap.put("amp", "&");
        replaceMap.put("apos", "'");
        replaceMap.put("gt", ">");
        replaceMap.put("lt", "<");
        replaceMap.put("quot", "\"");
    }

    //pre 标签的层数0-no >0 有
    private int preLevel = 0;
    private Reader reader;
    private int srcPos, srcCount;
    private char[] srcBuf;

    private int bufPos;
    private char[] buf;
    private char readItem = EOF, lastRead = EOF;
    private ContentHandler handler;

    public HtmlParser() {
    }

    public void setHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public void parase(InputStream is) throws IOException {
        if (handler == null) {
            throw new NullPointerException("you must set ContentHandler");
        }
        int len = is.available();
        len = len < 1024 ? 1024 : (len < 4096 ? 4096 : 6114);
        srcBuf = new char[len];
        buf = new char[(len >= 4096) ? 2048 : 1024];
        this.reader = new InputStreamReader(is, "UTF-8");
        srcPos = 0;
        srcCount = 0;
        parse();
    }

    public void parase(String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("input cant be null");
        }
        srcBuf = s.toCharArray();
        srcPos = 0;
        srcCount = srcBuf.length;
        int len = srcBuf.length;
        len = len < 2048 ? 1024 : 2048;
        buf = new char[len];
        parse();
    }

    //<!doctype html>
    //<!--注释-->
    private void parse() {
        if (handler == null) {
            return;
        } else {
            handler.startDocument(srcBuf.length);
        }

        read();
        while (readItem != EOF) {
            switch (readItem) {
                case EOF://end
                    handleStop();
                    break;
                case '<'://tags
                    read();
                    switch (readItem) {
                        case '/':
                            parseEndTag();
                            break;
                        case '!':
                            read();
                            if (readItem == '-') {
                                if (read() == '-') {
                                    parseComment();
                                } else if (readItem != '>') {
                                    skip();
                                }
                            } else {
                                skip();
                            }
                            break;
                        case '?':
                            skip();
                            break;
                        default:
                            parseStartTag();
                            break;
                    }
                    break;
                case '>':
                    //end tag
                    read();
                    parseText();
                    break;
                default:
                    if (lastRead == EOF || lastRead == '>') {
                        parseText();
                    } else {
                        read();
                    }
                    break;
            }
        }
    }

    //解析开始标签<a> <img /> <x a="b" c="d" e>
    private void parseStartTag() {
        if ((readItem < 'a' || readItem > 'z')
                && (readItem < 'A' || readItem > 'Z')) {
            //不合法的开始标签
            return;
        }

        //read name
        bufPos = 0;
        do {
            if (readItem >= 'A' && readItem <= 'Z') {
                readItem = (char) (readItem + 'a' - 'A');
            }
            buf[bufPos++] = readItem;
            read();
        } while (readItem != EOF && bufPos < MAX_TAG_LEN
                && ((readItem >= 'a' && readItem <= 'z')
                || (readItem >= 'A' && readItem <= 'Z')
                || (readItem >= '0' && readItem <= '9')));

        String name = new String(buf, 0, bufPos);
        int type = getTagType();

        if (bufPos == 3 && buf[0] == 'p')
            bufPos = 0;

        if (readItem == '/') {
            read();
        }

        String sttrs = "";
        if (readItem != '>') {
            if (readItem == ' ' || readItem == '\n') {
                readNoSpcBr();
            }

            if (readItem == '/') {
                read();
            }

            if (readItem != '>') {
                sttrs = parseAttrs();
            }
        }

        if (handler != null) {
            // TODO: 2017/2/6  
            handler.startElement(name, new HtmlNode(type));
        }
    }

    //解析属性值
    private String parseAttrs() {
        bufPos = 0;
        do {
            buf[bufPos++] = readItem;
            read();
        } while (readItem != EOF
                && readItem != '>'
                && bufPos < MAX_ATTR_LEN);

        if (buf[(bufPos - 1)] == '/') {
            bufPos--;
        }

        if (readItem != '>') {
            skip();
        }
        //// TODO: 2017/2/4 return new attrs
        return new String(buf, 0, bufPos);
    }

    //解析结束标签</xxx  > </xxx> </xxx\n  >
    private void parseEndTag() {
        bufPos = 0;
        while (readItem != EOF && (readItem = readNoSpcBr()) != '>') {
            if (bufPos >= MAX_TAG_LEN) {
                //不可能出现太长的tag
                break;
            } else {
                buf[bufPos++] = (char) readItem;
            }
        }

        String s = new String(buf, 0, bufPos);
        int type = getTagType();
        bufPos = 0;
        handler.endElement(type, s);
    }

    //解析注释
    private void parseComment() {
        while (readItem != EOF) {
            read();
            read();
            if (readItem == '-' && lastRead == '-') {
                read();
                if (readItem == '>') {
                    break;
                }
            }
        }
    }

    //解析文字
    private void parseText() {
        bufPos = 0;
        while (readItem != EOF && readItem != '<' && readItem != '>') {
            if (isInpre()) {//pre 标签 原封不动push
                pushText(readItem);
            } else {
                if (readItem == ' ' || readItem == '\n') {
                    if (bufPos != 0 && buf[(bufPos - 1)] != ' ') {
                        readItem = ' ';
                        pushText(readItem);
                    }
                } else {
                    pushText(readItem);
                }
            }
            read();
        }

        //不是空
        if (bufPos > 0 && handler != null) {
            //String s = new String(buf, 0, bufPos);
            handler.characters(buf, 0, bufPos);
        }
    }

    //处理解析完成
    private void handleStop() {
        // TODO: 2017/2/2 处理一些现在Stack里的内容
        readItem = EOF;
        handler.endDocument();
    }

    //读取一个字符
    private char read() {
        lastRead = readItem;
        if (srcPos < srcCount)
            readItem = srcBuf[srcPos++];
        else {
            if (reader == null) {
                readItem = EOF;
            } else {
                try {
                    srcCount = reader.read(srcBuf, 0, srcBuf.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    readItem = EOF;
                }
                if (srcCount <= 0)
                    readItem = EOF;
                else
                    readItem = srcBuf[0];
                srcPos = 1;
            }
        }

        switch (readItem) {
            case EOF:
                handleStop();
                break;
            case '\r':
                read();
                break;
        }
        return readItem;
    }

    //忽略读入的空格和回车
    private char readNoSpcBr() {
        while (readItem != EOF) {
            read();
            if (readItem != '\n' && readItem != ' ') {
                return readItem;
            }
        }
        return EOF;
    }

    //skip to next > or EOF
    private void skip() {
        while (readItem != EOF && readItem != '>') {
            read();
        }
    }

    //加入一个新的文字
    private void pushText(int c) {
        if (bufPos == buf.length) {
            char[] bigger = new char[bufPos * 4 / 3 + 4];
            System.arraycopy(buf, 0, bigger, 0, bufPos);
            buf = bigger;
        }

        buf[bufPos++] = (char) c;
    }

    //判断是否在pre标签内
    //pre 标签要保留格式
    private boolean isInpre() {
        return false;
    }

    private boolean equalTag(int start, String b) {
        int len = b.length();
        while (len-- != 0) {
            if (buf[start] != b.charAt(start))
                return false;
            start++;
        }

        return true;
    }

    private int getTagType() {
        switch (bufPos) {
            case 1:
                switch (buf[0]) {
                    case 'a':
                        return HtmlTag.A;
                    case 'b':
                        return HtmlTag.B;
                    case 'i':
                        return HtmlTag.I;
                    case 'p':
                        return HtmlTag.P;
                    case 'q':
                        return HtmlTag.Q;
                    case 's':
                        return HtmlTag.S;
                    case 'u':
                        return HtmlTag.U;
                }
                break;
            case 2:
                switch (buf[0]) {
                    case 'b':
                        if (buf[1] == 'r')
                            return HtmlTag.BR;
                        break;
                    case 'e':
                        if (buf[1] == 'r')
                            return HtmlTag.EM;
                        break;
                    case 'h':
                        if ('1' <= buf[1] && buf[1] <= '6') {
                            return (buf[1] - '1') + HtmlTag.H1;
                        } else if (buf[1] == 'r') {
                            return HtmlTag.HR;
                        }
                        break;
                    case 'l':
                        if (buf[1] == 'i')
                            return HtmlTag.LI;
                        break;
                    case 'o':
                        if (buf[1] == 'l')
                            return HtmlTag.OL;
                        break;
                    case 't':
                        if (buf[1] == 'd') {
                            return HtmlTag.TD;
                        } else if (buf[1] == 'h') {
                            return HtmlTag.TH;
                        } else if (buf[1] == 'r') {
                            return HtmlTag.TR;
                        } else if (buf[1] == 't') {
                            return HtmlTag.TT;
                        }
                        break;
                    case 'u':
                        if (buf[1] == 'l')
                            return HtmlTag.UL;
                        break;
                    default:
                        return HtmlTag.UNKNOWN;
                }
                break;
            case 3:
                switch (buf[0]) {
                    case 'b':
                        if (buf[1] == 'i' && buf[2] == 'g') {
                            return HtmlTag.BIG;
                        }
                        break;
                    case 'd':
                        if (buf[1] == 'e' && buf[2] == 'l') {
                            return HtmlTag.DEL;
                        } else if (buf[1] == 'f' && buf[2] == 'n') {
                            return HtmlTag.DFN;
                        } else if (buf[1] == 'i' && buf[2] == 'v') {
                            return HtmlTag.DIV;
                        }
                        break;
                    case 'i':
                        if (buf[1] == 'm' && buf[2] == 'g') {
                            return HtmlTag.IMG;
                        } else if (buf[1] == 'n' && buf[2] == 's') {
                            return HtmlTag.INS;
                        }
                        break;
                    case 'k':
                        if (buf[1] == 'b' && buf[2] == 'd') {
                            return HtmlTag.KBD;
                        }
                        break;
                    case 'p':
                        if (buf[1] == 'r') {
                            if (buf[2] == 'e') {
                                return HtmlTag.PRE;
                            }
                        }
                        break;
                    case 's':
                        if (buf[1] == 'u') {
                            if (buf[2] == 'b') {
                                return HtmlTag.SUB;
                            } else if (buf[2] == 'p') {
                                return HtmlTag.SUP;
                            }
                        }
                        break;
                    default:
                        return HtmlTag.UNKNOWN;
                }
                break;
            case 4:
                switch (buf[0]) {
                    case 'c':
                        if (buf[1] == 'i' && buf[2] == 't' && buf[3] == 'e') {
                            return HtmlTag.CITE;
                        } else if (buf[1] == 'o' && buf[2] == 'd' && buf[3] == 'e') {
                            return HtmlTag.CODE;
                        }
                        break;
                    case 'f':
                        if (buf[1] == 'o' && buf[2] == 'n' && buf[3] == 't') {
                            return HtmlTag.FONT;
                        }
                        break;
                    case 's':
                        if (buf[1] == 'p' && buf[2] == 'a' && buf[3] == 'n') {
                            return HtmlTag.SPAN;
                        }
                        break;
                    case 'm':
                        if (buf[1] == 'a' && buf[2] == 'r' && buf[3] == 'k') {
                            return HtmlTag.MARK;
                        }
                        break;
                    default:
                        return HtmlTag.UNKNOWN;
                }
                break;
            case 5:
                if (equalTag(2, "dio")) {
                    if (buf[0] == 'a' && buf[1] == 'u') {
                        return HtmlTag.AUDIO;
                    } else if (buf[0] == 'v' && buf[1] == 'e') {
                        return HtmlTag.VEDIO;
                    }
                } else {
                    switch (buf[0]) {
                        case 's':
                            if (equalTag(1, "mall")) {
                                return HtmlTag.SMALL;
                            }
                            break;
                        case 't':
                            if (equalTag(1, "able")) {
                                return HtmlTag.TABLE;
                            } else if (equalTag(1, "body")) {
                                return HtmlTag.TBODY;
                            } else if (equalTag(1, "head")) {
                                return HtmlTag.THEAD;
                            } else if (equalTag(1, "foot")) {
                                return HtmlTag.TFOOT;
                            }
                            break;
                        default:
                            return HtmlTag.UNKNOWN;
                    }
                }
                break;
            case 6:
                if (equalTag(0, "str")) {
                    if (buf[3] == 'o' && buf[4] == 'n' && buf[5] == 'g') {
                        return HtmlTag.STRONG;
                    } else if (buf[3] == 'i' && buf[4] == 'k' && buf[5] == 'e') {
                        return HtmlTag.STRIKE;
                    }
                } else if (buf[4] == 'e' && buf[5] == 'f') {
                    if (buf[0] == 'h' && buf[1] == 'e' && buf[2] == 'a' && buf[3] == 'd') {
                        return HtmlTag.HEADER;
                    } else if (buf[0] == 'f' && buf[1] == 'o' && buf[2] == 'o' && buf[3] == 't') {
                        return HtmlTag.FOOTER;
                    }
                }
                break;
            case 7:
                if (equalTag(0, "caption")) {
                    return HtmlTag.CAPTION;
                }
                break;
            case 10:
                if (equalTag(0, "blockquote")) {
                    return HtmlTag.BLOCKQUOTE;
                }
                break;
            default:
                return HtmlTag.UNKNOWN;
        }

        return HtmlTag.UNKNOWN;
    }

}
