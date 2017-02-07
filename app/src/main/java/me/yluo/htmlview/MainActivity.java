package me.yluo.htmlview;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView t = (TextView) findViewById(R.id.text);

        int i = t.getWidth();
        int j = t.getMaxWidth();
        int k = t.getMeasuredWidth();

        String text = "";

        try {
            InputStream in = getAssets().open("TestHtml.html");
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            text = new String(buffer);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HtmlView.parseHtml(text).into(t);
    }

    //Html.fromHtml()
}
