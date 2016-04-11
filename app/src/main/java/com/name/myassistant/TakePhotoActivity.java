package com.name.myassistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.name.myassistant.util.CompressImageUtil;
import com.name.myassistant.util.TakePhoto;

public abstract class TakePhotoActivity extends AppCompatActivity implements TakePhoto.TakeResultListener,CompressImageUtil.CompressListener{
    private TakePhoto takePhoto;
    protected ProgressDialog wailLoadDialog;

    /**
     *  获取TakePhoto实例
     * @return
     */
    public TakePhoto getTakePhoto(){
        if (takePhoto==null){
            takePhoto=new TakePhoto(this,this);
        }
        return takePhoto;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (takePhoto!=null)outState.putParcelable("imageUri", takePhoto.getImageUri());
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        getTakePhoto().setImageUri((Uri)savedInstanceState.getParcelable("imageUri"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 压缩照片
     * @param path 照片路径
     */
    protected void compressPic(String path) {
        wailLoadDialog = showProgressDialog(TakePhotoActivity.this, "正在压缩照片...");// 提交数据
        new CompressImageUtil().compressImageByPixel(path, this);
    }
    @Override
    public void onCompressSuccessed(String imgPath) {
        if (wailLoadDialog!=null)wailLoadDialog.dismiss();
    }
    @Override
    public void onCompressFailed(String msg) {
        if (wailLoadDialog!=null)wailLoadDialog.dismiss();
    }



    /**
     * 显示圆形进度对话框
     *
     * @author JPH
     * @date 2014-12-12 下午7:04:09
     * @param activity
     * @param progressTitle
     *            显示的标题
     * @return
     */
    public ProgressDialog showProgressDialog(final Activity activity,
                                                    String... progressTitle) {
        if(activity==null||activity.isFinishing())return null;
        String title = "提示";
        if (progressTitle != null && progressTitle.length > 0)
            title = progressTitle[0];
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }
}
