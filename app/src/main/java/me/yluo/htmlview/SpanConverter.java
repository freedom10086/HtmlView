package me.yluo.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import java.io.IOException;
import java.util.Stack;

import me.yluo.htmlview.spann.Bold;
import me.yluo.htmlview.spann.Code;
import me.yluo.htmlview.spann.Heading;
import me.yluo.htmlview.spann.Hr;
import me.yluo.htmlview.spann.Image;
import me.yluo.htmlview.spann.Italic;
import me.yluo.htmlview.spann.Li;
import me.yluo.htmlview.spann.Link;
import me.yluo.htmlview.spann.Pre;
import me.yluo.htmlview.spann.Quote;
import me.yluo.htmlview.spann.Strike;
import me.yluo.htmlview.spann.Sub;
import me.yluo.htmlview.spann.Super;
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
                handleBlockTag(node.type, false);
                break;
            case HtmlTag.IMG:
                handleImage(position, "www.baidu.com", 0);
                break;
            case HtmlTag.HR:
                handleHr(position);
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
                setSpan(start, new Li());
                break;
            case HtmlTag.PRE:
                setSpan(start, new Pre());
                break;
            case HtmlTag.BLOCKQUOTE:
                handleBlockquote(start);
                break;
            case HtmlTag.Q:
            case HtmlTag.CODE:
            case HtmlTag.KBD:
                setSpan(start, new Code());
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
                setSpan(start, new Sub());
                break;
            case HtmlTag.SUP:
                setSpan(start, new Super());
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

    private void handleHr(int start) {
        spannedBuilder.append(' ');
        position++;

        spannedBuilder.setSpan(new Hr(), start, position,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private void setSpan(int start, Object span) {
        spannedBuilder.setSpan(span, start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
