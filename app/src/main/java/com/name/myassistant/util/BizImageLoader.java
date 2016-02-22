package com.name.myassistant.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created by xu on 16-1-28.
 */
public class BizImageLoader {
    private static BizImageLoader bizImageLoader=null;
    private BizImageLoader(){}
    public synchronized static BizImageLoader getBizImageLoader(){
        if(null==bizImageLoader){
            initialize();
        }
        return bizImageLoader;
    }
    public static void initialize() {
        bizImageLoader = new BizImageLoader();
    }
    public void loadWithUrl(String url, ImageView target) {
        Glide.with(target.getContext())
                .load(url)
                .into(target);
    }

    public void loadWithBitmap(Bitmap bitmap,ImageView target){

        Glide.with(target.getContext())
                .load(bitmap)
                .into(target);
    }

    public Bitmap getBitmapWithUrl(String url,Context context){
        Bitmap bitmap = null;
        try {
            bitmap =  Glide.with(context).load(url).asBitmap().into(200,200).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从sd卡里缓存图片
     *
     * @param uri
     * @param target
     */
    public void loadFromSd(File uri, ImageView target) {
        Glide.with(target.getContext())
                .load(uri)
                .asBitmap()
                .centerCrop()
                .into(target);
    }

    public RequestManager with(Context context) {
        return Glide.with(context);
    }

    public RequestManager with(Activity activity) {
        return Glide.with(activity);
    }

    public RequestManager with(FragmentActivity activity) {
        return Glide.with(activity);
    }

    public RequestManager with(AppCompatActivity activity) {
        return Glide.with(activity);
    }

    public RequestManager with(Fragment fragment) {
        return Glide.with(fragment);
    }

    public RequestManager with(android.support.v4.app.Fragment fragment) {
        return Glide.with(fragment);
    }
}
