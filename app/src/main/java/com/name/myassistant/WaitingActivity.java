package com.name.myassistant;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.name.myassistant.qoa.Qa;
import com.name.myassistant.util.LocalDisplay;

public class WaitingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        ImageView imageView=(ImageView)findViewById(R.id.robot);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(imageView, "scaleX", 0.5f, 1.0f),
                ObjectAnimator.ofFloat(imageView, "scaleY", 0.5f, 1.0f)
        );

        set.setDuration(700).start();
        new initTask().execute(this);
    }



    class initTask extends AsyncTask<Context,Void,Void> {
        @Override
        protected Void doInBackground(Context... params) {
            Context context=params[0];
            LocalDisplay.init(context);

            //恢复闹钟和读取短信许可的数据
            GlobalVariable.recoverData(context);

            if(GlobalVariable.getInstance().getAlarmList().size()>0){
                //如果有闹钟，就设置开机自启动闹钟设置
                ComponentName receiver = new ComponentName(context, BootReceiver.class);
                PackageManager pm = context.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }else{
                //如果没闹钟，就取消开机自启动闹钟设置
                ComponentName receiver = new ComponentName(context, BootReceiver.class);
                PackageManager pm = context.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }

            //初始化语言处理的数据
            Qa.initData(context);
            return null;
        }
        @Override
        protected void onPostExecute(Void avoid) {
            Intent intent=new Intent(WaitingActivity.this,MainActivity.class);
            WaitingActivity.this.startActivity(intent);
            finish();
        }
    }
}
