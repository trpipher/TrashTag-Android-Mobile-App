package com.trashtag.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class AppResources {
    public static Bitmap getBitmapfromVector(Context context, int drawableID)
    {
        Drawable d = ContextCompat.getDrawable(context,drawableID);
        int w = d.getIntrinsicWidth()/2, h = d.getIntrinsicHeight()/2;
        Bitmap bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0,0,w,h);
        d.draw(canvas);
        return bitmap;
    }

}
