package com.name.myassistant.shortMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.name.myassistant.GlobalVariable;
import com.name.myassistant.MainActivity;
import com.name.myassistant.R;
import com.name.myassistant.util.LogUtil;

/**
 * 短信广播接收
 */

public class SmsReceiver extends BroadcastReceiver {
    private  static SmsListener mSmsListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage msg;

        String robotOutputStr;
        String phoneNum="";
        String info="";
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                phoneNum=phoneNum+msg.getOriginatingAddress();
                info=info+msg.getDisplayMessageBody();

                LogUtil.d("xzx", "number:" + msg.getOriginatingAddress()
                        + "   body:" + msg.getDisplayMessageBody() + "  time:"
                        + msg.getTimestampMillis());
            }
            robotOutputStr=phoneNum+context.getString(R.string.short_message_tip)+info;

            if(GlobalVariable.getInstance().isREAD_SHORT_MESSAGE_PERMISSION()){
                if(mSmsListener==null){
                    Intent intent1=new Intent(context, MainActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.putExtra("robotOutputStr",robotOutputStr);
                    context.startActivity(intent1);
                }else{
                    mSmsListener.onReceive(robotOutputStr);
                }
            }
        }
    }

    public interface SmsListener {
        void onReceive(String msg);
    }

    public static void setmSmsListener(SmsListener smsListener){
        mSmsListener = smsListener;
    }
}
