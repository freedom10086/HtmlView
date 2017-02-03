package me.yluo.htmlview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;

public class MainActivity extends Activity {

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
        HtmlParser parser = new HtmlParser();
        InputStream is = getAssets().open("TestHtml.html");
        parser.setInput(is);
        parser.parse();
    }
}
