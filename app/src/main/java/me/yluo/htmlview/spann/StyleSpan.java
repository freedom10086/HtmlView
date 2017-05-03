package me.yluo.htmlview.spann;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import me.yluo.htmlview.AttrParser;

//如果值为-1代表没有
// TODO: 2017/3/2 支持更多的style
public class StyleSpan extends CharacterStyle
        implements UpdateAppearance {

    private static final float[] TEXT_SIZE = new float[]{
            0.75f, 1.0f, 1.15f, 1.3f, 1.45f, 1.6f, 1.75f
    };

    private int color;
    private int fontSize;

    public StyleSpan(int color, int fontSize) {
        this.color = color;
        this.fontSize = fontSize;
    }


    @Override
    public void updateDrawState(TextPaint tp) {
        if (color != AttrParser.COLOR_NONE) {
            tp.setColor(color);
        }

//        if (bgClolr >= 0) {
//            tp.bgColor = bgClolr;
//        }

        if (fontSize > 0) {
            if (fontSize > TEXT_SIZE.length) {
                fontSize = TEXT_SIZE.length;
            }
            tp.setTextSize(tp.getTextSize() * TEXT_SIZE[fontSize - 1]);
        }

    }
}
