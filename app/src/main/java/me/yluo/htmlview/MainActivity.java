package me.yluo.htmlview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.InputStream;

public class MainActivity extends Activity implements ContentHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView t = (TextView) findViewById(R.id.text);

        try {
            testPull();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        try {
            //TestHtml.html TestXml.xml
            InputStream in = getAssets().open("TestHtml.html");
            int size = in.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();

            // Convert the buffer into a string.
            String text = new String(buffer);

            t.setText(Html.fromHtml(text));

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    //android.text.Html

    private void testPull() throws Exception {
        HtmlParser parser = new HtmlParser(this);
        InputStream is = getAssets().open("TestHtml.html");
        parser.parase(is);
    }

    @Override
    public void startDocument() {
        Log.e("HTML", "===START===");
    }

    @Override
    public void endDocument() {
        Log.e("HTML", "===END===");
    }

    @Override
    public void startElement(int type, String name, String atts) {
        Log.e("HTML", "<" + name + (atts == null ? "" : " " + atts) + ">");
    }

    @Override
    public void endElement(int type, String name) {
        Log.e("HTML", "<" + name + "/>");
    }

    @Override
    public void characters(char[] ch, int start, int len) {
        String s = new String(ch, start, len);
        Log.e("HTML", s);
    }
}
