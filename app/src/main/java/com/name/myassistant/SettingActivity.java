package com.name.myassistant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.name.myassistant.deskclock.DeskClockMainActivity;
import com.name.myassistant.util.LogUtil;

import java.io.File;

public class SettingActivity extends TakePhotoActivity implements View.OnClickListener{
    RoundImageView imageView;
    LinearLayout setImgLayout;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_setting);

        Toolbar toolbar=(Toolbar)findViewById(R.id.robot_setting_toolbar);
        imageView=(RoundImageView)findViewById(R.id.user_img);
        CheckBox readShortMessageCheckBox=(CheckBox)findViewById(R.id.read_short_message_check);
        CheckBox allowToSayCheckBox=(CheckBox)findViewById(R.id.set_allow_to_say);
        TextView alarmSettingTextView=(TextView)findViewById(R.id.alarm_setting);
//        TextView setUserImgTextView=(TextView)findViewById(R.id.set_img_for_user);
        setImgLayout=(LinearLayout)findViewById(R.id.set_img_layout);
        TextView takePhotoTextView=(TextView)findViewById(R.id.take_photo);
        TextView selectPictureTextView=(TextView)findViewById(R.id.select_picture);
        TextView noImgTextView=(TextView)findViewById(R.id.no_img);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        readShortMessageCheckBox.setChecked(GlobalVariable.getInstance().isREAD_SHORT_MESSAGE_PERMISSION());
        allowToSayCheckBox.setChecked(GlobalVariable.getInstance().isAllowToSay());
        alarmSettingTextView.setOnClickListener(this);
        imageView.setOnClickListener(this);
        takePhotoTextView.setOnClickListener(this);
        selectPictureTextView.setOnClickListener(this);
        noImgTextView.setOnClickListener(this);

//        final SharedPreferences sharedPreferences = getSharedPreferences("myassistant", MODE_PRIVATE);
        
        readShortMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtil.d("xzx", "readShortMessage=> " + isChecked);
                GlobalVariable.getInstance().setREAD_SHORT_MESSAGE_PERMISSION(SettingActivity.this, isChecked);


//                sharedPreferences.edit().putBoolean("readShortMessagePermission", isChecked).apply();
            }
        });
        allowToSayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalVariable.getInstance().setAllowToSay(isChecked);
                GlobalVariable.save(SettingActivity.this);
            }
        });

        setUserImg();

    }

    @Override
    public void onBackPressed() {
        if(setImgLayout.getVisibility()==View.VISIBLE){
            setImgLayout.setVisibility(View.INVISIBLE);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.alarm_setting:
                Intent intent=new Intent(SettingActivity.this,DeskClockMainActivity.class);
                startActivity(intent);
                break;
            case R.id.user_img:
                setImgLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.take_photo:
                LogUtil.d("xzx","imageUri=> "+imageUri.toString());
                this.getTakePhoto().picTakeCrop(imageUri);
                break;
            case R.id.select_picture:
                LogUtil.d("xzx","imageUri=> "+imageUri.toString());
                getTakePhoto().picSelectCrop(imageUri);
                break;
            case R.id.no_img:
                GlobalVariable.getInstance().setUSER_HAS_IMG(false);
                GlobalVariable.save(SettingActivity.this);
                setUserImg();
                setImgLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


    @Override
    public void takeSuccess(Uri uri) {
        GlobalVariable.getInstance().setUSER_HAS_IMG(true);
        GlobalVariable.save(this);
        LogUtil.d("xzx","uri=> "+uri.getPath()+" => "+uri.toString());

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 5;
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath(), option);
        imageView.setImageBitmap(bitmap);
        setImgLayout.setVisibility(View.GONE);
    }

    @Override
    public void takeFail(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void takeCancel() {

    }

    void setUserImg(){
        boolean userHasImg=GlobalVariable.getInstance().isUSER_HAS_IMG();
        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.user_img_tag) + ".jpg");
        imageUri = Uri.fromFile(file);
        if(userHasImg){
            LogUtil.d("xzx");
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 5;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), option);
            imageView.setImageBitmap(bitmap);
        }else{
            LogUtil.d("xzx");
            imageView.setImageResource(R.drawable.user_img);
        }
    }
}
