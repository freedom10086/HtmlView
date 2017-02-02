package me.yluo.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class Html {
    private Html() {
    }

    /**
     * 处理图片标签/本地图片or网络图片
     * Make sure you call
     * setBounds() on your Drawable if it doesn't already have
     * its bounds set.
     */
    public static interface ImageGetter {
        Drawable getDrawable(String source);
    }

    public static interface TagHandler {
        void handleTag(boolean opening, String tag,
                       Editable output, XMLReader xmlReader);
    }

    /**
     * 渲染html到spanned
     */
    public static Spanned fromHtml(String source) {
        return fromHtml(source, null, null);
    }

    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        XMLReader xmlReader = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            //parser.setProperty();//parser.setProperty(, HtmlParser.schema);
            //获取事件源
            xmlReader = parser.getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        ToSpannedConverter converter =
                new ToSpannedConverter(source, imageGetter, tagHandler, xmlReader);
        return converter.convert();
    }
}
