package me.yluo.htmlview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DefaultImageGetter implements HtmlView.ImageGetter, Callback {

    private Context context;
    private int maxWidth;//最大宽度 图片不要大于这个值
    private static OkHttpClient client;
    private final static WeakHashMap<String, Drawable> caches = new WeakHashMap<>();
    private final static WeakHashMap<Call, TaskInfo> tasks = new WeakHashMap<>();

    public DefaultImageGetter(int maxWidth, Context context) {
        this.context = context;
        this.maxWidth = maxWidth;

    }

    /**
     * 压缩图片质量:
     * bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
     * 其中的quality为0~100, 可以压缩图片质量, 不过对于大图必须对图片resize
     * <p>
     * 这个是等比例缩放:
     * bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
     * newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, width, height, matrix, true);//用距阵的方式缩放
     * <p>
     * 这个是截取图片某部分:
     * bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
     * <p>
     * <p>
     * mDrawable = mContext.getDrawable(resourceId);
     * mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(),
     * mDrawable.getIntrinsicHeight());
     * <p>
     * mDrawable = new BitmapDrawable(context.getResources(), b);
     * int width = mDrawable.getIntrinsicWidth();
     * int height = mDrawable.getIntrinsicHeight();
     * mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
     * <p>
     * Drawable d = context.getDrawable(R.drawable.test1);
     */

    @Override
    public void getDrawable(String source, int start, int end, ImageGetterCallBack callBack) {
        if (callBack == null) return;

        //缓存有
        if (caches.containsKey(source)) {
            callBack.onImageReady(source, start, end, caches.get(source));
            return;
        }

        if (isLocal()) {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.test1);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            if (width > maxWidth) {
                //缩放图片
                float scale = maxWidth * 1.0f / width;
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                height = (int) (height * scale);
                width = maxWidth;
                bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
            }

            Drawable d = new BitmapDrawable(context.getResources(), bmp);
            d.setBounds(0, 0, width, height);
            callBack.onImageReady(source, start, end, d);
        } else {
            //Future<?> future = getExecutorService().submit(localFileImageLoader);
            Request builder = new Request.Builder().url(source).get().build();
            Call call = getClient().newCall(builder);
            tasks.put(call, new TaskInfo(source, start, end, callBack));
            call.enqueue(this);
        }

    }


    @Override
    public void onFailure(Call call, IOException e) {
        TaskInfo info = tasks.remove(call);
        if (info == null) return;
        info.done(info.source, null);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        TaskInfo info = tasks.get(call);
        if (info == null) return;
        try {
            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            Drawable d = new BitmapDrawable(context.getResources(), bitmap);
            d.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            info.done(info.source, d);

            tasks.remove(call);
        } catch (IOException e) {
            onFailure(call, e);
        }
    }

    private boolean isLocal() {
        return true;
    }

    private class TaskInfo {
        String source;
        private int start;
        private int end;
        private ImageGetterCallBack callBack;

        TaskInfo(String source, int start, int end, ImageGetterCallBack callBack) {
            this.source = source;
            this.start = start;
            this.end = end;
            this.callBack = callBack;
        }

        void done(String s, Drawable d) {
            if (d == null) {
                d = getPlaceHolder(s);
            } else {
                caches.put(s, d);
            }
            callBack.onImageReady(source, start, end, d);
        }
    }

    private Drawable getPlaceHolder(String souce) {
        return null;
    }

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }
}
