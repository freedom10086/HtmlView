package me.yluo.htmlview.spann;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import me.yluo.htmlview.HtmlView;


public class Link extends URLSpan {

    //    color: #4078c0;
    //text-decoration: none;

    private final String url;

    public Link(String url) {
        super(url);
        this.url = url;
    }

    @Override
    public void onClick(View widget) {
        System.out.println("link clcik");
        Uri uri = Uri.parse(url);
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w("URLSpan", "Actvity was not found for intent, " + intent.toString());
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(HtmlView.URL_COLOR);
        ds.setUnderlineText(false);
    }

}
