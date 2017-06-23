package com.meitu.mopicamera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 图片操作工具类
 * Created by libiao on 2017/6/21.
 */
public class BitmapUtil {

    /**
     * 采样压缩大图
     * @param path
     * @param targetW
     * @param targetH
     * @return
     */
    public static Bitmap scaleFromSdcard(String path, int targetW, int targetH){
        // 获取option
        BitmapFactory.Options options = new BitmapFactory.Options();
        // inJustDecodeBounds设置为true,这样使用该option decode出来的Bitmap是null，
        // 只是把长宽存放到option中
        options.inJustDecodeBounds = true;
        // 此时bitmap为null
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int inSampleSize = 1; // 1是不缩放
        // 计算宽高缩放比例
        int inSampleSizeW = options.outWidth / targetW;
        int inSampleSizeH = options.outHeight / targetH;
        if (inSampleSizeW > inSampleSizeH) {
            inSampleSize = inSampleSizeW;
        }else {
            inSampleSize = inSampleSizeH;
        }
        // 设置缩放比例
        options.inSampleSize = inSampleSize;
        // 一定要记得将inJustDecodeBounds设为false，否则Bitmap为null
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }
}
