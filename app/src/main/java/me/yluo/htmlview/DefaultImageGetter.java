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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


public class DefaultImageGetter implements HtmlView.ImageGetter {

    private static final String TAG = DefaultImageGetter.class.getSimpleName();
    private Context context;
    private ImageCacher imageCacher;
    private int maxWidth;//最大宽度 图片不要大于这个值

    private static Set<BitmapWorkerTask> taskCollection;

    static {
        taskCollection = new HashSet<>();
    }


    public DefaultImageGetter(int maxWidth, Context context) {
        this.context = context;
        this.maxWidth = maxWidth;
        imageCacher = ImageCacher.instance(context.getCacheDir() + "/imageCache/");
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
     */

    @Override
    public void getDrawable(String source, int start, int end, ImageGetterCallBack callBack) {
        if (callBack == null) return;
        Bitmap b = imageCacher.get(source);
        if(b==null){
            if (isLocal()) {
                Bitmap bmp = decodeBitmapFromRes(context.getResources(), R.drawable.test1,maxWidth,Integer.MAX_VALUE);
                Drawable d = new BitmapDrawable(context.getResources(), bmp);
                d.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
                callBack.onImageReady(source, start, end, d);
            } else {
                //// TODO: 2017/2/15  
            }
        }
        
        callBack.onImageReady(source, start, end, b);
    }


    private boolean isLocal() {
        return true;
    }


    private class ImageInfo {
        String source;
        private int start;
        private int end;
        private ImageGetterCallBack callBack;

        ImageInfo(String source, int start, int end, ImageGetterCallBack callBack) {
            this.source = source;
            this.start = start;
            this.end = end;
            this.callBack = callBack;
        }

        void done(String s, Drawable d) {
            callBack.onImageReady(source, start, end, d);
        }
    }

    private Drawable getPlaceHolder(String souce) {
        return null;
    }


    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }

    public static Bitmap decodeBitmapFromRes(Resources res, int resId, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateScale(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is);
        options.inSampleSize = calculateScale(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    public static Bitmap decodeBitmapFromFile(String name, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(name, options);
        options.inSampleSize = calculateScale(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(name, options)
    }


    public static int calculateScale(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int srcHeight = options.outHeight;
        final int srcWidth = options.outWidth;
        int scale = 1;
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) srcHeight / (float) reqHeight);
            final int widthRatio = Math.round((float) srcWidth / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return scale;
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
    }

    private static Bitmap downloadBitmap(String imageUrl) {
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(4 * 1000);
            con.setReadTimeout(8 * 1000);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bitmap;
    }

    /**
     * 异步下载图片的任务。
     */
    private class BitmapWorkerTask implements Runnable {
        private String imageUrl;

        @Override
        public void run() {
            taskCollection.add(this);
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            try {
                final URL url = new URL(imageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 4 * 1024);
                //下载的同时存到硬盘缓存中
                out = new BufferedOutputStream(imageCacher.newDiskCacheStream(imageUrl), 4 * 1024);
                int len;
                byte[] bb = new byte[4 * 1024];
                while ((len = in.read(bb)) != -1) {
                    out.write(bb, 0, len);
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

            Bitmap bitmap = decodeBitmapFromFile();
            taskCollection.remove(this);
        }


    }
}
