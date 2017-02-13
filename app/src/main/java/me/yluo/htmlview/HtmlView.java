package me.yluo.htmlview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.WindowManager;
import android.widget.TextView;


public class HtmlView {
    public static final float LINE_HEIGHT = 1.4f;
    public static final int TEXT_COLOR = 0xff333333;
    public static final int URL_COLOR = 0xff4078c0;
    public static float FONT_SIZE = 40;
    public static int VIEW_WIDTH = 1080;


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
            WindowManager wm = (WindowManager) textView.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            VIEW_WIDTH = p.x - textView.getPaddingStart() - textView.getPaddingEnd();
            imageGetter = new DefaultImageGetter(VIEW_WIDTH, textView.getContext());
        }

        FONT_SIZE = textView.getTextSize();
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
     * start image 开始位置
     * end 结束位置
     */
    public interface ImageGetter {
        void getDrawable(String source, int start, int end, ImageGetterCallBack callBack);

        interface ImageGetterCallBack {
            void onImageReady(String source, int start, int end, Drawable d);
        }
    }
}
