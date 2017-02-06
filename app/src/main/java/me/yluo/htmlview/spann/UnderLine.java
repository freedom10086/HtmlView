package me.yluo.htmlview.spann;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class UnderLine extends CharacterStyle {
    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setUnderlineText(true);
    }
}
