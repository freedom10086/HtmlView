package me.yluo.htmlview;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;

import me.yluo.htmlview.callback.SpanClickListener;

/**
 * 链接点击事件处理内
 */
public class DefaultClickHandler implements SpanClickListener {
    private static final String TAG = DefaultClickHandler.class.getSimpleName();

    private Context context;

    public DefaultClickHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onSpanClick(int type, String source) {
        Log.d(TAG, "span click type is " + type + " source is:" + source);
        switch (type) {
            case HtmlTag.A:
                Uri uri = Uri.parse(source);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w("URLSpan", "Actvity was not found for intent, " + intent.toString());
                }
                break;
            case HtmlTag.IMG:
                break;
            default:
                break;
        }
    }
}
