package me.yluo.htmlview;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Stack;

public class KXmlParser {

    private Hashtable replaceMap = new Hashtable() {{
        replaceMap.put("amp", "&");
        replaceMap.put("apos", "'");
        replaceMap.put("gt", ">");
        replaceMap.put("lt", "<");
        replaceMap.put("quot", "\"");
    }};

    private boolean running = true;
    private Stack<String> tagStack = new Stack<>();
    private ContentHandler contentHandler;
    // source
    private Reader reader;
    private char[] srcBuf;
    private int readItem = -1;

    private int srcPos;
    private int srcCount;

    // txtbuffer
    private char[] txtBuf = new char[128];
    private int txtPos;

    private int line = 1, column = 0;

    public KXmlParser() {
        srcBuf = new char[Runtime.getRuntime().freeMemory() >= 1048576 ? 8192 : 512];
    }

    public void setInput(InputStream is) throws XmlPullParserException {
        try {
            setInput(new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setInput(Reader reader) throws XmlPullParserException {
        this.reader = reader;
        line = 1;
        column = 0;
        if (reader == null)
            return;
        srcPos = 0;
        srcCount = 0;
    }

    //<!doctype html>
    //<!--注释-->
    private void parse() throws IOException {
        readItem = read();
        do {
            switch (readItem) {
                case '<':
                    readItem = read();
                    if (readItem == '/') {
                        parseEndTag();
                    } else {
                        parseStartTag();
                    }
                    break;
                case '!':
                    parseComment();
                    break;
                default:
                    parseText();
                    break;
            }
        } while (running);
    }

    private void parseStartTag() {
        String s = parseName();
        if (readItem != '>') {
            parseAttrs();
        }

        contentHandler.startElement(s, null);
    }

    private final String parseName() {
        if ((readItem < 'a' || readItem > 'z')
                && (readItem < 'A' || readItem > 'Z')
                && readItem != '_'
                && readItem != ':'
                && readItem < 0x0c0) {
            // TODO: 2017/2/2 error tag
        }

        StringBuilder sb = new StringBuilder(readItem);

        while (readItem != ' ' && readItem != '\n' && readItem != '>') {
            if (txtPos < txtBuf.length) {
                //(char)((a   > >   24)   &   0xff);
                txtBuf[txtPos++] = (char) readItem;
            } else {
                sb.append(txtBuf);
                txtPos = 0;
            }
        }

        sb.append(txtBuf, 0, txtPos);
        txtPos = 0;

        return sb.toString();
    }

    private void parseAttrs() {
        // TODO: 2017/2/2
        readItem = readNoSpcBr();
        while (readItem != ' ') {
            readItem = readNoSpcBr();
        }
    }

    private void parseEndTag() {
        StringBuilder sb = new StringBuilder();
        while ((readItem = readNoSpcBr()) != '>') {
            if (txtPos < txtBuf.length) {
                //(char)((a   > >   24)   &   0xff);
                txtBuf[txtPos++] = (char) readItem;
            } else {
                sb.append(txtBuf);
                txtPos = 0;
            }
        }

        sb.append(txtBuf, 0, txtPos);
        txtPos = 0;
        contentHandler.endElement(sb.toString());
    }

    private void parseComment() {
        for (; read() != '>'; ) ;
    }

    private void parseText() {
        StringBuilder sb = new StringBuilder();
        if (readItem != '>') {
            sb.append(readItem);
        }
        while (readItem != '<') {
            if (txtPos < txtBuf.length) {
                txtBuf[txtPos++] = (char) readItem;
            } else {
                sb.append(txtBuf);
                txtPos = 0;
            }
        }

        sb.append(txtBuf, 0, txtPos);
        txtPos = 0;
        contentHandler.characters(sb);

    }

    private void handleStop() {
        // TODO: 2017/2/2 处理一些现在Stack里的内容
        contentHandler.endDocument();
        running = false;
    }

    private int read() {
        if (srcPos < srcCount) {
            readItem = srcBuf[srcPos++];
        } else {
            try {
                srcCount = reader.read(srcBuf, 0, srcBuf.length);
            } catch (IOException e) {
                e.printStackTrace();
                handleStop();
            }

            if (srcCount <= 0)
                readItem = -1;
            else
                readItem = srcBuf[0];
        }

        column++;
        if (readItem == '\n') {
            line++;
            column = 1;
        }

        if (readItem == -1) {
            handleStop();
        }
        return readItem;
    }

    private int readNoSpcBr() {
        int r = -1;
        while (true) {
            r = read();
            if (r != '\n' && r != ' ') {
                return r;
            }
        }

    }
}
