package me.yluo.htmlview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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


public class ImageGetter implements HtmlView.ImageGetter {

    private static final String TAG = ImageGetter.class.getSimpleName();
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


    public ImageGetter(int maxWidth, Context context) {
        this.context = context;
        this.maxWidth = maxWidth;
        imageCacher = ImageCacher.instance(context.getCacheDir() + "/imageCache/");
    }


    @Override
    public void getDrawable(String source, int start, int end, ImageGetterCallBack callBack) {
        if (callBack == null) return;
        Bitmap b = imageCacher.getMemCache(source);

        if (b == null) {
            if (isLocal()) {//本地图片不缓存到硬盘
                b = decodeBitmapFromRes(context.getResources(), R.drawable.test1, maxWidth);
            } else {
                //网络图片再检查硬盘缓存
                b = decodeBitmapFromStream(imageCacher.getDiskCacheStream(source), maxWidth);
                if (b == null && !mPool.isShutdown()) {
                    mPool.execute(new BitmapWorkerTask(source, start, end, callBack));
                }
            }

            if (b != null) {
                b = scaleBitmap(b, maxWidth);
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
        return true;
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
            isCancel = false;
        }

        @Override
        public void run() {
            taskCollection.add(this);
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            Bitmap bitmap = null;
            try {
                final URL url = new URL(imageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 4 * 1024);
                bitmap = decodeBitmapFromStream(imageCacher.getDiskCacheStream(imageUrl), maxWidth);
                if (bitmap != null && !isCancel) {
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

                    //存到内存之前需要压缩
                    bitmap = scaleBitmap(bitmap, maxWidth);
                    imageCacher.putMemCache(imageUrl, bitmap);
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
                //如果下载失败就不用返回了
                //因为之前以前有holder了
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

    private Drawable getPlaceHolder(String souce) {
        return null;
    }

    public static Bitmap decodeBitmapFromRes(Resources res, int resId, int reqWidth) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = options.outWidth / reqWidth;
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmapFromStream(InputStream is, int reqWidth) {
        if (is == null) return null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is);
        options.inSampleSize = options.outWidth / reqWidth;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > maxWidth) {//缩放图片
            float scale = maxWidth * 1.0f / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            height = (int) (height * scale);
            width = maxWidth;
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return bitmap;
        /**
         * 压缩图片质量:
         * bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
         * 其中的quality为0~100, 可以压缩图片质量, 不过对于大图必须对图片resize
         * 这个是等比例缩放:
         * bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
         * newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, width, height, matrix, true);//用距阵的方式缩放
         * 这个是截取图片某部分:
         * bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
         */
    }
}
