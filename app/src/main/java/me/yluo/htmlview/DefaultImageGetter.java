package me.yluo.htmlview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.yluo.htmlview.callback.ImageGetter;
import me.yluo.htmlview.callback.ImageGetterCallBack;


public class DefaultImageGetter implements ImageGetter {

    private static final String TAG = DefaultImageGetter.class.getSimpleName();
    private Context context;
    private ImageCacher imageCacher;
    private int maxWidth;//最大宽度 图片不要大于这个值
    private static Set<BitmapWorkerTask> taskCollection;
    private static ExecutorService mPool;

    static {
        taskCollection = new HashSet<>();
        if (mPool == null) {
            int thread = Runtime.getRuntime().availableProcessors();
            mPool = Executors.newFixedThreadPool(thread);
        }
    }


    public DefaultImageGetter(int maxWidth, Context context) {
        this.context = context;
        this.maxWidth = maxWidth;
        imageCacher = ImageCacher.instance(context.getCacheDir() + "/imageCache/");
    }


    @Override
    public void getDrawable(String source, int start, int end, ImageGetterCallBack callBack) {
        if (callBack == null) return;
        Log.d(TAG, "get getDrawable " + source);
        Bitmap b = imageCacher.getMemCache(source);

        if (b == null) {
            if (isLocal()) {
                //本地图片不缓存到硬盘
                b = decodeBitmapFromRes(context.getResources(), R.drawable.test1, maxWidth);
            } else {
                //网络图片再检查硬盘缓存
                b = BitmapFactory.decodeStream(imageCacher.getDiskCacheStream(source));
                b = scaleBitmap(b, maxWidth);
                if (b != null) {
                    Log.d(TAG, "get image from diskcache " + source);
                }

                //没有缓存去下载
                if (b == null && !mPool.isShutdown()) {
                    mPool.execute(new BitmapWorkerTask(source, start, end, callBack));
                }
            }

            //放到内存缓存
            if (b != null) {
                imageCacher.putMemCache(source, b);
            }
        }

        callBack.onImageReady(source, start, end, bmpToDrawable(source, b));
    }

    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask t : taskCollection) {
                t.cancel();
            }
        }

        if (mPool != null && !mPool.isShutdown()) {
            synchronized (mPool) {
                mPool.shutdownNow();
            }
        }
    }

    private boolean isLocal() {
        return false;
    }

    //图片下载及存储
    private class BitmapWorkerTask implements Runnable {
        private String imageUrl;
        private boolean isCancel;
        private int start, end;
        private ImageGetterCallBack callBack;

        public BitmapWorkerTask(String imageUrl, int start, int end, ImageGetterCallBack callBack) {
            this.imageUrl = imageUrl;
            this.start = start;
            this.end = end;
            this.callBack = callBack;
        }

        public void cancel() {
            isCancel = true;
        }

        @Override
        public void run() {
            taskCollection.add(this);
            Log.d(TAG, "start download image " + imageUrl);
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            Bitmap bitmap = null;
            try {
                final URL url = new URL(imageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 4 * 1024);
                //bitmap = decodeBitmapFromStream(in, maxWidth);
                bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null && !isCancel) {
                    Log.d(TAG, "download image compete " + imageUrl);
                    //存到硬盘
                    Bitmap.CompressFormat f = Bitmap.CompressFormat.PNG;
                    if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") ||
                            imageUrl.endsWith(".JPG") || imageUrl.endsWith(".JPEG")) {
                        f = Bitmap.CompressFormat.JPEG;
                    } else if (imageUrl.endsWith(".webp")) {
                        f = Bitmap.CompressFormat.WEBP;
                    }

                    out = new BufferedOutputStream(imageCacher.newDiskCacheStream(imageUrl), 4 * 1024);
                    bitmap.compress(f, 90, out);

                    Log.d(TAG, "image init width is " + bitmap.getWidth());
                    //存到内存之前需要压缩
                    bitmap = scaleBitmap(bitmap, maxWidth);
                    Log.d(TAG, "after scale image width is " + bitmap.getWidth());
                    imageCacher.putMemCache(imageUrl, bitmap);
                } else {
                    Log.d(TAG, "download image error " + imageUrl);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            taskCollection.remove(this);
            if (!isCancel && bitmap != null) {
                //如果下载失败就不用返回了 因为之前以前有holder了
                callBack.onImageReady(imageUrl, start, end, bmpToDrawable(imageUrl, bitmap));
            }
        }
    }

    //永远不要返回null
    public Drawable bmpToDrawable(String source, Bitmap b) {
        if (b == null) {
            return getPlaceHolder(source);
        } else {
            Drawable d = new BitmapDrawable(context.getResources(), b);
            d.setBounds(0, 0, b.getWidth(), b.getHeight());
            return d;
        }
    }

    //// TODO: 2017/2/22
    private Drawable getPlaceHolder(String souce) {
        ColorDrawable colorDrawable = new ColorDrawable(0xffcccccc);
        colorDrawable.setBounds(0, 0, 500, 300);
        return colorDrawable;
    }


    public static Bitmap decodeBitmapFromStream(InputStream is, int reqWidth) {
        if (is == null) return null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeStream(is, null, options);
        return scaleBitmap(src, reqWidth);
    }

    public static Bitmap decodeBitmapFromRes(Resources res, int resId, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置成了true,不占用内存，只获取bitmap宽高
        BitmapFactory.decodeResource(res, resId, options); // 第一次解码
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options);
        return scaleBitmap(src, reqWidth);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        // 源图片的高度和宽度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap scaleBitmap(Bitmap src, int dstWidth) {
        if (src == null) return null;
        int srcWidth = src.getWidth();
        if (srcWidth <= dstWidth) return src;

        float scale = dstWidth * 1.0f / srcWidth;
        int dstHeight = (int) (scale * src.getHeight());

        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }
}

/**
 * 笔记 android 分辨率和dpi关系
 * ldpi	    120dpi	0.75
 * mdpi	    160dpi	1
 * hdpi	    240dpi	1.5
 * xhdpi    320dpi	2     1280*720   1dp=2px
 * xxhdpi： 480dpi  3     1920*1080 1dp=3px
 * xxxhdpi  640dpi  4
 */
