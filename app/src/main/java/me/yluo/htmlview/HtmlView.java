package me.yluo.htmlview;

import android.content.Context;
import android.graphics.Point;
import android.text.Spanned;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import me.yluo.htmlview.callback.ImageGetter;
import me.yluo.htmlview.callback.ViewChangeNotify;


public class HtmlView implements ViewChangeNotify {

    private static final String TAG = HtmlView.class.getSimpleName();
    public static final float LINE_HEIGHT = 1.4f;
    public static final int TEXT_COLOR = 0xff333333;
    public static final int URL_COLOR = 0xff4078c0;
    public static float FONT_SIZE = 40;
    public static int VIEW_WIDTH = 1080;

    private String source;
    private ImageGetter imageGetter;
    private boolean isViewSet;
    private WeakReference<TextView> target;
    private Spanned spanned;

    private HtmlView(String source) {
        this.source = source;
        isViewSet = false;
    }

    public static HtmlView parseHtml(String source) {
        return new HtmlView(source);
    }

    public HtmlView setImageGetter(ImageGetter imageGetter) {
        this.imageGetter = imageGetter;
        return this;
    }

    public void into(TextView target) {
        if (this.target == null) {
            this.target = new WeakReference<>(target);
        }
        if (imageGetter == null) {
            WindowManager wm = (WindowManager) target.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            VIEW_WIDTH = p.x - target.getPaddingStart() - target.getPaddingEnd();
            imageGetter = new DefaultImageGetter(VIEW_WIDTH, target.getContext());
        }

        FONT_SIZE = target.getTextSize();
        spanned = SpanConverter.convert(source, imageGetter, this);
        target.setTextColor(TEXT_COLOR);
        target.setLineSpacing(0, LINE_HEIGHT);
        target.setText(spanned);
        isViewSet = true;
    }


    @Override
    public void notifyViewChange() {
        if (target == null) return;
        final TextView t = target.get();

        if (isViewSet && t != null && spanned != null) {
            t.post(new Runnable() {
                @Override
                public void run() {
                    t.setText(spanned);
                    Log.d(TAG, "notifyViewChange postInvalidateDelayed");
                }
            });
        } else {
            Log.d(TAG, "notifyViewChange is not set view do nothing");
        }
    }
}
