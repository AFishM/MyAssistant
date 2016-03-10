package com.name.myassistant.alarm;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.name.myassistant.R;
import com.name.myassistant.util.LogUtil;

public class AlarmSettingActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    int fragmentLayoutId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);
        setTitle("");
        Toolbar toolbar=(Toolbar)findViewById(R.id.alarm_setting_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fragmentLayoutId=R.id.alarm_setting_layout;
        fragmentManager=getFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.add(fragmentLayoutId,new AlarmListFragment(),AlarmListFragment.class.toString());
        transaction.addToBackStack(AlarmListFragment.class.toString());
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d("xzx");
        if(fragmentManager.getBackStackEntryCount()>1){
            LogUtil.d("xzx");
            fragmentManager.popBackStack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alarm_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.d("xzx");
        if(item.getItemId()==R.id.plus_address){
            LogUtil.d("xzx");
            FragmentTransaction transaction=fragmentManager.beginTransaction();
            transaction.add(fragmentLayoutId,new AlarmSettingFragment(),AlarmSettingFragment.class.toString());
            transaction.addToBackStack(AlarmSettingFragment.class.toString());
            transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }
}
