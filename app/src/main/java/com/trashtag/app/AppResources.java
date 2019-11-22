package com.trashtag.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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


    public static FirebaseAuth mAuth(){return FirebaseAuth.getInstance();}
    public static DatabaseReference databaseReference(){return FirebaseDatabase.getInstance().getReference();}
    public static DatabaseReference databaseReference(String path){return FirebaseDatabase.getInstance().getReference(path);}

}
