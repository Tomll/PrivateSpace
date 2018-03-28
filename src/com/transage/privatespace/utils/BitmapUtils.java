package com.transage.privatespace.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by yanjie.xu on 2017/9/28.
 */

public class BitmapUtils {
    /**
     * 将二进制数组转换成图片
     * @param array
     * @return
     */
    public static Bitmap byteArray2Bitmap(byte[] array){
        if (array == null || array.length <= 0) {
            return null;
        }

        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        input = new ByteArrayInputStream(array);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
//        if (photoData != null) {
//            photoData = null;
//        }
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
//        ByteArrayInputStream var6 = new ByteArrayInputStream(photoData);
//        Bitmap photo = BitmapFactory.decodeStream(var6);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
//        return bitmap;
    }
}
