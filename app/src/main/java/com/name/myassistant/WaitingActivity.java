package com.name.myassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.name.myassistant.qoa.Qa;

public class WaitingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        new initTask().execute(this);
    }



    class initTask extends AsyncTask<Context,Void,Void> {
        @Override
        protected Void doInBackground(Context... params) {
            SharedPreferences sharedPreferences=getSharedPreferences("myassistant",MODE_PRIVATE);
            AppConfig.READ_SHORT_MESSAGE_PERMISSION=sharedPreferences.getBoolean("readShortMessagePermission", false);
            AppConfig.REPORT_WEATHER_ON_TIME=sharedPreferences.getBoolean("reportWeatherOnTime",false);
            Qa.initData(params[0]);
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
