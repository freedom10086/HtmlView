package me.yluo.htmlview;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import java.io.IOException;
import java.util.Stack;

import me.yluo.htmlview.callback.ImageGetter;
import me.yluo.htmlview.callback.ImageGetterCallBack;
import me.yluo.htmlview.callback.SpanClickListener;
import me.yluo.htmlview.callback.ViewChangeNotify;
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
import me.yluo.htmlview.spann.StyleSpan;
import me.yluo.htmlview.spann.Sub;
import me.yluo.htmlview.spann.Super;
import me.yluo.htmlview.spann.UnderLine;

public class SpanConverter implements ParserCallback, ImageGetterCallBack {
    private static final String TAG = SpanConverter.class.getSimpleName();
    private String mSource;
    private SpannableStringBuilder spannedBuilder;
    private ImageGetter imageGetter;
    private SpanClickListener clickListener;
    private HtmlParser parser;
    private Stack<HtmlNode> nodes;
    private ViewChangeNotify notify;
    private int position;

    private SpanConverter(String source, ImageGetter imageGetter,
                          SpanClickListener listener, ViewChangeNotify notify) {
        mSource = source;
        this.imageGetter = imageGetter;
        this.clickListener = listener;
        parser = new HtmlParser();
        nodes = new Stack<>();
        parser.setHandler(this);
        position = 0;
        this.notify = notify;
    }

    public static Spanned convert(String source, ImageGetter imageGetter,
                                  SpanClickListener listener, ViewChangeNotify notify) {

        SpanConverter converter = new SpanConverter(source, imageGetter, listener, notify);
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
        Log.d(TAG, "startDocument");
        spannedBuilder = new SpannableStringBuilder();
    }

    @Override
    public void startElement(HtmlNode node) {
        Log.d(TAG, "startElement " + node.name);
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
                handleImage(position, node.attr.src);
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
        Log.d(TAG, "characters " + new String(ch, start, length));
        spannedBuilder.append(new String(ch, start, length));
        position += length;
        //还要根据栈顶的元素类型添加适当的\n
    }

    @Override
    public void endElement(int type, String name) {
        Log.d(TAG, "endElement " + name);
        if (type == HtmlTag.UNKNOWN
                || type == HtmlTag.BR
                || type == HtmlTag.IMG
                || type == HtmlTag.HR
                || nodes.isEmpty()) {
            return;
        }

        if (nodes.peek().type != type) {
            return;
        }

        HtmlNode node = nodes.pop();
        int start = node.start;

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
                handleStyle(start, node.attr);
                break;
            case HtmlTag.B:
            case HtmlTag.STRONG:
                setSpan(start, new Bold());
                break;
            case HtmlTag.A:
                handleUrl(start, node.attr.href);
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

    @Override
    public void endDocument() {
        Log.d(TAG, "endDocument");
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

    private void handleStyle(int start, HtmlNode.HtmlAttr attr) {
        if (attr == null) return;
        spannedBuilder.setSpan(new StyleSpan(attr.color, -1), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleBlockquote(int start) {
        spannedBuilder.setSpan(new Quote(), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleUrl(int start, String url) {
        spannedBuilder.setSpan(new Link(url, clickListener), start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void handleImage(int start, String url) {
        spannedBuilder.append("\uFFFC");
        position++;

        if (imageGetter != null) {
            imageGetter.getDrawable(url, start, position, this);
        }
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


    @Override
    public void onImageReady(String source, int start, int end, Drawable d) {
        Log.d(TAG, "onImageReady: " + source + " position: " + start + "," + end);
        Image[] is = spannedBuilder.getSpans(start, end, Image.class);
        for (Image i : is) {
            spannedBuilder.removeSpan(i);
        }

        spannedBuilder.setSpan(new Image(source, d), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        notify.notifyViewChange();

    }
}
