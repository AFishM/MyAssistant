package com.name.myassistant.shortMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.name.myassistant.GlobalVariable;
import com.name.myassistant.MainActivity;
import com.name.myassistant.util.LogUtil;

/**
 * 短信广播接收
 */

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage msg;
        String info="";
        String phoneNum="";
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                info+=msg.getDisplayMessageBody();
                LogUtil.d("xzx", "number:" + msg.getOriginatingAddress()
                        + "   body:" + msg.getDisplayMessageBody() + "  time:"
                        + msg.getTimestampMillis());
                phoneNum=msg.getOriginatingAddress();
            }
            Intent intent1=new Intent(context, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("phoneNum",phoneNum);
            intent1.putExtra("info",info);
            context.startActivity(intent1);
            if(GlobalVariable.getInstance().isREAD_SHORT_MESSAGE_PERMISSION()){
//                Intent intent1=new Intent(context, MainActivity.class);
//                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent1.putExtra("phoneNum",phoneNum);
//                intent1.putExtra("info",info);
//                context.startActivity(intent1);
            }
        }
    }
}
