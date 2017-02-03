package me.yluo.htmlview;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;

public class HtmlParser {

    private static Hashtable replaceMap = new Hashtable();
    private static final int MAX_TAG_LEN = 16;
    private static final int MAX_ATTR_LEN = 256;

    static {
        replaceMap.put("amp", "&");
        replaceMap.put("apos", "'");
        replaceMap.put("gt", ">");
        replaceMap.put("lt", "<");
        replaceMap.put("quot", "\"");
    }

    private boolean running = true;
    // source
    private Reader reader;
    private int srcPos, srcCount;
    private char[] srcBuf;

    private int tempBufPos;
    private char[] tempBuf = new char[512];
    private int readItem, lastRead = -1;
    private int line = 1, column = 0;


    public void setInput(InputStream is) throws IOException {
        int len = is.available();
        len = len < 1024 ? 1024 : (len < 4096 ? 4096 : 8192);
        srcBuf = new char[len];
        this.reader = new InputStreamReader(is, "UTF-8");
        line = 1;
        column = 0;
        srcPos = 0;
        srcCount = 0;
    }

    //<!doctype html>
    //<!--注释-->
    public void parse() {
        while (running) {
            switch (read()) {
                case -1://end
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
                        default:
                            parseStartTag();
                            break;
                    }
                    break;
                default://text

                    break;
            }
        }
    }

    //解析开始标签<a> <img /> <x a="b" c="d" e>
    private void parseStartTag() {
        if ((readItem < 'a' || readItem > 'z')
                && (readItem < 'A' || readItem > 'Z')
                && readItem != '_'
                && readItem != ':') {
            //不合法的开始标签
            return;
        }

        //read name
        tempBufPos = 0;
        do {
            if (readItem >= 'A' && readItem <= 'Z') {
                readItem = readItem + 'a' - 'A';
            }
            tempBuf[tempBufPos++] = (char) readItem;
            read();
        } while (running && tempBufPos < MAX_TAG_LEN
                && ((readItem >= 'a' && readItem <= 'z')
                || (readItem >= 'A' && readItem <= 'Z')
                || (readItem >= '0' && readItem <= '9')));

        String name = new String(tempBuf, 0, tempBufPos);
        tempBufPos = 0;

        while (readItem == ' ' || readItem == '\n') {
            read();
        }

        // TODO: 2017/2/3
        if (readItem == '/') {
            read();
        }

        if (readItem == '>' || readItem == -1) {
            //end
            System.out.println("tag name:<" + name + ">");
        } else {
            System.out.println("tag name:<" + name + " attrs " + ">");
            parseAttrs();
        }
        //contentHandler.startElement(s, null);
    }


    //解析属性值
    private Attibutes parseAttrs() {
        tempBufPos = 0;
        do {
            tempBuf[tempBufPos++] = (char) readItem;
            read();
        } while (running
                && readItem != '>'
                && tempBufPos < MAX_ATTR_LEN);

        if (tempBuf[(tempBufPos - 1)] == '/') {
            tempBufPos--;
        }

        if (readItem != '>') {
            skip();
        }

        return new Attibutes(new String(tempBuf, 0, tempBufPos));
    }

    //解析结束标签</xxx  > </xxx> </xxx\n  >
    private void parseEndTag() {
        tempBufPos = 0;
        while (running && (readItem = readNoSpcBr()) != '>') {
            if (tempBufPos >= MAX_TAG_LEN) {
                //不可能出现太长的tag
                break;
            } else {
                tempBuf[tempBufPos++] = (char) readItem;
            }
        }

        String s = new String(tempBuf, 0, tempBufPos);
        tempBufPos = 0;
        System.out.println("end tag:</" + s + ">");
        //contentHandler.endElement(sb.toString());
    }

    //解析注释
    private void parseComment() {
        while (running) {
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
        StringBuilder sb = new StringBuilder();
        if (readItem != '>') {
            sb.append(readItem);
        }
        while (readItem != '<') {
            if (tempBufPos < tempBuf.length) {
                tempBuf[tempBufPos++] = (char) readItem;
            } else {
                sb.append(tempBuf);
                tempBufPos = 0;
            }
        }

        sb.append(tempBuf, 0, tempBufPos);
        tempBufPos = 0;
        //contentHandler.characters(sb);

    }

    //处理解析完成
    private void handleStop() {
        // TODO: 2017/2/2 处理一些现在Stack里的内容
        //contentHandler.endDocument();
        running = false;
    }

    //读取一个字符
    private int read() {
        lastRead = readItem;
        if (srcPos < srcCount)
            readItem = srcBuf[srcPos++];
        else {
            try {
                srcCount = reader.read(srcBuf, 0, srcBuf.length);
            } catch (IOException e) {
                e.printStackTrace();
                readItem = -1;
            }

            if (srcCount <= 0)
                readItem = -1;
            else
                readItem = srcBuf[0];
            srcPos = 1;
        }

        column++;
        switch (readItem) {
            case -1:
                handleStop();
                break;
            case '\r':
                read();
                break;
            case '\n':
                line++;
                column = 1;
                break;
        }
        return readItem;
    }

    //忽略读入的空格和回车
    private int readNoSpcBr() {
        while (running) {
            read();
            if (readItem != '\n' && readItem != ' ') {
                return readItem;
            }
        }
        return -1;
    }

    //skip to next > or EOF
    private void skip() {
        while (running) {
            read();
            if (readItem == '>' || readItem == -1) {
                break;
            }
        }
    }
}
