package me.yluo.htmlview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class DefaultImageGetter implements HtmlView.ImageGetter {

    private Context context;
    private int maxWidth;//最大宽度 图片不要大于这个值

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
     */

    @Override
    public Drawable getDrawable(String source) {
        /**
         * mDrawable = mContext.getDrawable(resourceId);
         mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(),
         mDrawable.getIntrinsicHeight());

         mDrawable = new BitmapDrawable(context.getResources(), b);
         int width = mDrawable.getIntrinsicWidth();
         int height = mDrawable.getIntrinsicHeight();
         mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

         Drawable d = context.getDrawable(R.drawable.test1);
         */

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
        return d;
    }
}
