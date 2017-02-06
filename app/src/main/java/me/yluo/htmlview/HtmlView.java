package me.yluo.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.widget.TextView;


public class HtmlView {
    public static final float LINE_HEIGHT = 1.5f;
    public static final int TEXT_COLOR = 0xff333333;
    public static final int URL_COLOR = 0xff4078c0;


    private String source;
    private ImageGetter imageGetter;

    private HtmlView(String source) {
        this.source = source;
    }

    public static HtmlView parseHtml(String source) {
        return new HtmlView(source);
    }

    public HtmlView setImageGetter(ImageGetter imageGetter) {
        this.imageGetter = imageGetter;
        return this;
    }

    public void into(TextView textView) {
        if (imageGetter == null) {
            imageGetter = new DefaultImageGetter();
        }

        Spanned spanned = SpanConverter.convert(source, imageGetter);
        textView.setTextColor(TEXT_COLOR);
        textView.setLineSpacing(0, LINE_HEIGHT);
        textView.setText(spanned);
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
}
