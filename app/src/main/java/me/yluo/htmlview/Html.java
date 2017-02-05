package me.yluo.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;


public class Html {
    private Html() {
    }

    /**
     * 处理图片标签/本地图片or网络图片
     * Make sure you call
     * setBounds() on your Drawable if it doesn't already have
     * its bounds set.
     */
    public interface ImageGetter {
        Drawable getDrawable(String source);
    }

    public interface TagHandler {
        void handleTag(boolean opening, String tag, Editable output);
    }

    /**
     * 渲染html到spanned
     */
    public static Spanned fromHtml(String source) {
        return fromHtml(source, null, null);
    }

    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        HtmlParser parser = new HtmlParser();
        SpanConverter converter =
                new SpanConverter(source, imageGetter, tagHandler, parser);
        return converter.convert();
    }
}
