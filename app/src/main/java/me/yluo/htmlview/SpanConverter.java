package me.yluo.htmlview;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.Log;

import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Stack;

import me.yluo.htmlview.spann.Bold;
import me.yluo.htmlview.spann.Heading;
import me.yluo.htmlview.spann.Image;
import me.yluo.htmlview.spann.Italic;
import me.yluo.htmlview.spann.Link;
import me.yluo.htmlview.spann.Quote;
import me.yluo.htmlview.spann.Strike;
import me.yluo.htmlview.spann.UnderLine;

public class SpanConverter implements ContentHandler {
    private String mSource;
    private SpannableStringBuilder spannedBuilder;
    private HtmlView.ImageGetter imageGetter;
    private HtmlParser parser;
    private Stack<HtmlNode> nodes;
    private int position;

    private SpanConverter(String source, HtmlView.ImageGetter imageGetter) {
        mSource = source;
        this.imageGetter = imageGetter;
        parser = new HtmlParser();
        nodes = new Stack<>();
        parser.setHandler(this);
        position = 0;
    }

    public static Spanned convert(String source, HtmlView.ImageGetter imageGetter) {
        SpanConverter converter = new SpanConverter(source, imageGetter);
        return converter.startConvert();
    }

    private Spanned startConvert() {
        try {
            parser.parase(mSource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spannedBuilder;
    }

    @Override
    public void startDocument(int len) {
        Log.e("==", "startDocument");
        spannedBuilder = new SpannableStringBuilder();
    }


    @Override
    public void endDocument() {
        Log.e("==", "endDocument");
    }

    @Override
    public void startElement(String name, HtmlNode node) {
        Log.e("==", "startElement " + name);
        if (HtmlTag.isBolckTag(node.type)) {
            handleBlockTag(node.type, true);
        }
        switch (node.type) {
            case HtmlTag.UNKNOWN:
                break;
            case HtmlTag.BR:
                break;
            case HtmlTag.IMG:
                handleImage(position, "www.baidu.com", 0);
                break;
            case HtmlTag.HR:
                handleBlockTag(node.type, false);
                break;
            default:
                node.start = position;
                nodes.push(node);
                break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        Log.e("==", "characters " + new String(ch, start, length));
        spannedBuilder.append(new String(ch, start, length));
        position += length;
        //还要根据栈顶的元素类型添加适当的\n
    }

    @Override
    public void endElement(int type, String name) {
        Log.e("==", "endElement " + name);
        if (type == HtmlTag.UNKNOWN
                || type == HtmlTag.BR
                || type == HtmlTag.IMG
                || type == HtmlTag.HR) {
            return;
        }

        int start = 0;
        if (!nodes.isEmpty() && nodes.peek().type == type) {
            start = nodes.pop().start;
        }

        switch (type) {
            case HtmlTag.H1:
            case HtmlTag.H2:
            case HtmlTag.H3:
            case HtmlTag.H4:
            case HtmlTag.H5:
            case HtmlTag.H6:
                handleHeading(start, type - HtmlTag.H1 + 1);
                break;
            case HtmlTag.P:
                break;
            case HtmlTag.B:
            case HtmlTag.STRONG:
                setSpan(start, new Bold());
                break;
            case HtmlTag.A:
                handleUrl(start, "");
                break;
            case HtmlTag.I:
            case HtmlTag.EM:
            case HtmlTag.CITE:
            case HtmlTag.DFN:
                setSpan(start, new Italic());
                break;
            case HtmlTag.DEL:
            case HtmlTag.S:
            case HtmlTag.STRIKE:
                setSpan(start, new Strike());
                break;
            case HtmlTag.U:
            case HtmlTag.INS:
                setSpan(start, new UnderLine());
                break;
            case HtmlTag.UL:
            case HtmlTag.OL:
            case HtmlTag.DIV:
            case HtmlTag.HEADER:
            case HtmlTag.FOOTER:
                break;
            case HtmlTag.LI:
                break;
            case HtmlTag.PRE:
                break;
            case HtmlTag.BLOCKQUOTE:
                handleBlockquote(start);
                break;
            case HtmlTag.Q:
                break;
            case HtmlTag.CODE:
            case HtmlTag.KBD:
                break;
            case HtmlTag.MARK:
                break;
            case HtmlTag.SPAN:
                break;
            case HtmlTag.FONT:
                break;
            case HtmlTag.BIG:
                break;
            case HtmlTag.SMALL:
                break;
            case HtmlTag.SUB:
                break;
            case HtmlTag.SUP:
                break;
            case HtmlTag.TT:
                //Monospace
                break;
            case HtmlTag.TABLE:
            case HtmlTag.CAPTION:
            case HtmlTag.THEAD:
            case HtmlTag.TFOOT:
            case HtmlTag.TBODY:
            case HtmlTag.TH:
            case HtmlTag.TD:
                break;
        }

        if (HtmlTag.isBolckTag(type)) {
            handleBlockTag(type, false);
        }
    }

    private void handleBlockTag(int type, boolean start) {
        if (position <= 0) return;
        if (spannedBuilder.charAt(position - 1) != '\n') {
            spannedBuilder.append('\n');
            position++;
        }
    }

    //level h1-h6
    private void handleHeading(int start, int level) {
        spannedBuilder.setSpan(new Heading(level), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //spannedBuilder.setSpan(new StyleSpan(Typeface.BOLD), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleBlockquote(int start) {
        spannedBuilder.setSpan(new Quote(), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleUrl(int start, String url) {
        spannedBuilder.setSpan(new Link("http://www.baidu.com/"), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleImage(int start, String url, int maxWidth) {
        Drawable d = null;

        if (imageGetter != null) {
            d = imageGetter.getDrawable(url);
        }

        spannedBuilder.append("\uFFFC");
        position++;

        spannedBuilder.setSpan(new Image(url, d), start, position,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private void setSpan(int start, Object span) {
        spannedBuilder.setSpan(span, start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private void startLi(Editable text, Attributes attributes) {
        //startBlockElement(text, attributes);
        start(text, new Bullet());
        startCssStyle(text, attributes);
    }

    private static void endLi(Editable text) {
        endCssStyle(text);
        //endBlockElement(text);
        end(text, Bullet.class, new BulletSpan());
    }


    private static <T> T getLast(Spanned text, Class<T> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void setSpanFromMark(Spannable text, Object mark, Object... spans) {
        int where = text.getSpanStart(mark);
        text.removeSpan(mark);
        int len = text.length();
        if (where != len) {
            for (Object span : spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private static void end(Editable text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        if (obj != null) {
            setSpanFromMark(text, obj, repl);
        }
    }

    private void startCssStyle(Editable text, Attributes attributes) {
        /*
        String style = attributes.getValue("", "style");
        if (style != null) {
            Matcher m = getForegroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Foreground(c | 0xFF000000));
                }
            }

            m = getBackgroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Background(c | 0xFF000000));
                }
            }

            m = getTextDecorationPattern().matcher(style);
            if (m.find()) {
                String textDecoration = m.group(1);
                if (textDecoration.equalsIgnoreCase("line-through")) {
                    start(text, new Strikethrough());
                }
            }
        }
        */
    }

    private static void endCssStyle(Editable text) {
        Strikethrough s = getLast(text, Strikethrough.class);
        if (s != null) {
            setSpanFromMark(text, s, new StrikethroughSpan());
        }

        Background b = getLast(text, Background.class);
        if (b != null) {
            setSpanFromMark(text, b, new BackgroundColorSpan(b.mBackgroundColor));
        }

        Foreground f = getLast(text, Foreground.class);
        if (f != null) {
            setSpanFromMark(text, f, new ForegroundColorSpan(f.mForegroundColor));
        }
    }

    private static void startImg(Editable text, Attributes attributes, HtmlView.ImageGetter img) {
        String src = attributes.getValue("", "src");
        Drawable d = null;

        if (img != null) {
            d = img.getDrawable(src);
        }

        if (d == null) {
            d = new ColorDrawable(Color.GRAY);
            //d.getIntrinsicWidth(), d.getIntrinsicHeight()
            d.setBounds(0, 0, 200, 200);
        }

        int len = text.length();
        text.append("\uFFFC");

        text.setSpan(new ImageSpan(d, src), len, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void startFont(Editable text, Attributes attributes) {
        /*
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        if (!TextUtils.isEmpty(color)) {
            int c = getHtmlColor(color);
            if (c != -1) {
                start(text, new Foreground(c | 0xFF000000));
            }
        }

        if (!TextUtils.isEmpty(face)) {
            start(text, new Font(face));
        }
        */
    }

    private static void endFont(Editable text) {
        Font font = getLast(text, Font.class);
        if (font != null) {
            setSpanFromMark(text, font, new TypefaceSpan(font.mFace));
        }

        Foreground foreground = getLast(text, Foreground.class);
        if (foreground != null) {
            setSpanFromMark(text, foreground,
                    new ForegroundColorSpan(foreground.mForegroundColor));
        }
    }

    private static void startA(Editable text, Attributes attributes) {
        String href = attributes.getValue("", "href");
        start(text, new Href(href));
    }

    private static void endA(Editable text) {
        Href h = getLast(text, Href.class);
        if (h != null) {
            if (h.mHref != null) {
                setSpanFromMark(text, h, new URLSpan((h.mHref)));
            }
        }
    }

    private static class Strikethrough {
    }

    private static class Blockquote {
    }


    private static class Bullet {
    }

    private static class Font {
        public String mFace;

        public Font(String face) {
            mFace = face;
        }
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    private static class Foreground {
        private int mForegroundColor;

        public Foreground(int foregroundColor) {
            mForegroundColor = foregroundColor;
        }
    }

    private static class Background {
        private int mBackgroundColor;

        public Background(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }
    }

}
