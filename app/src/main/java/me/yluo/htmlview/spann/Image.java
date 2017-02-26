package me.yluo.htmlview.spann;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import java.lang.ref.WeakReference;

import me.yluo.htmlview.HtmlView;

public class Image extends ReplacementSpan {

    private String source;
    private Drawable mDrawable;
    private WeakReference<Drawable> mDrawableRef;
    private static final float LINE_HEIGHT = 1.0f / HtmlView.LINE_HEIGHT;
    private static final int PADDING = (int) HtmlView.FONT_SIZE / 5;

    public Image(String source, Drawable d) {
        this.source = source;
        mDrawable = d;
    }


    public Drawable getDrawable() {
        return mDrawable;
    }

    //是否为内联
    //如果为行内图片就不需要考虑line height
    private boolean isInline() {
        return false;
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }

    //返回宽度
    //可以设置fm 设置高度
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            if (isInline()) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;
            } else {
                //图片单独一行
                //重新计算line height
                fm.ascent = -(int) (rect.bottom * LINE_HEIGHT) - PADDING;
                fm.descent = (int) (HtmlView.FONT_SIZE * (HtmlView.LINE_HEIGHT - 1)) + PADDING;
            }

            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }

        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();

        int transY = 0;
        if (isInline()) {//base line对其
            transY = bottom - b.getBounds().bottom;
            transY -= paint.getFontMetricsInt().descent;
        } else {
            //单独一行上对其
            transY = top + PADDING;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
