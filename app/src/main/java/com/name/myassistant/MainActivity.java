package com.name.myassistant;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.view.MotionEventCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.name.myassistant.m.Chat;
import com.name.myassistant.qoa.Qa;
import com.name.myassistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener,View.OnLongClickListener{

    ImageView inputSwitchImageView;
    TextView pressToSayTextView;
    EditText userInputEditText;

    ChatContentListViewAdapter chatContentListViewAdapter;

    boolean isInputWithSay=true;
    boolean isAllowRobotToSay;

    //SpeechRecognizer 语音听写对象
    SpeechRecognizer mIat;
    //SpeechSynthesizer 语音合成对象
    SpeechSynthesizer mTts;

    String recognizerStr;

    //听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener(){
        //听写结果回调接口(返回Json格式结果,用户可参见附录12.1);
        //一般情况下会通过onResults接口多次返回结果,完整的识别内容是多次结果的累加;
        //关于解析Json的代码可参见MscDemo中JsonParser类;
        //isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            recognizerStr=recognizerStr+parseJsonToString(results.getResultString());
            inputSwitchImageView.setImageResource(R.drawable.keyboard_32);
            pressToSayTextView.setVisibility(View.GONE);
            userInputEditText.setVisibility(View.VISIBLE);
            userInputEditText.setText(recognizerStr);
            isInputWithSay=false;
        }
        //会话发生错误回调接口
        public void onError(SpeechError error) {
            LogUtil.d("xzx","SpeechError=> "+error.toString());
            error.getPlainDescription(true);//获取错误码描述
        }

        //音量值0~30
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        //开始录音
        public void onBeginOfSpeech() {}

        //结束录音
        public void onEndOfSpeech() {}
        //扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口,没有错误时,error为null
        public void onCompleted(SpeechError error) {
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }

        //缓冲进度回调
        //percent为缓冲进度0~100,beginPos为缓冲音频在文本中开始位置,endPos表示缓冲音频在文本中结束位置,info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }
        //会话事件回调接口
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ListView chatContentListView=(ListView)findViewById(R.id.chat_content);
        inputSwitchImageView=(ImageView)findViewById(R.id.input_switch);
        pressToSayTextView=(TextView)findViewById(R.id.press_to_say);
        userInputEditText =(EditText)findViewById(R.id.question_input);
        TextView sendTextView=(TextView)findViewById(R.id.send);

        List<Chat> chatList=new ArrayList<>();
        chatContentListViewAdapter=new ChatContentListViewAdapter(chatList);
        Bitmap userBitmap =BitmapFactory.decodeResource(getResources(),R.drawable.user_img_48);
        chatContentListViewAdapter.setUserImgBitmap(userBitmap);
        chatContentListViewAdapter.setRobotImgBitmap(userBitmap);

        chatContentListView.setAdapter(chatContentListViewAdapter);

        inputSwitchImageView.setOnClickListener(this);

        pressToSayTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        //开始听写
                        pressToSayTextView.setText(getString(R.string.loosen_to_end));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_light_gray_solid);
                        mIat.startListening(mRecoListener);
                        recognizerStr = "";
                        return true;
                    case MotionEvent.ACTION_UP:
                        pressToSayTextView.setText(getString(R.string.press_and_say));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_gray);
                        if (mIat.isListening()) {
                            mIat.stopListening();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        sendTextView.setOnClickListener(this);

        //初始化，创建语音配置对象
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.app_id));
//        analyze("天空为什么是蓝的");
//        analyze("香港什么时候回归");
//        analyze("香港在哪里");
//        analyze("广外校长是谁");
//        analyze("中国的首都是什么");
//        analyze("咖啡怎么做");
//        analyze("中国有多少个省");
//        analyze("珠穆朗玛峰有多高");
//        userInputEditText.setText("广外校长是谁");

        initSpeechRecognizer();
        initSpeechSynthesizer();

        String info=getIntent().getStringExtra("info");
        if(info!=null){
            wakeUpAndUnlock();
            Chat chat=new Chat(false,info);
            chatContentListViewAdapter.chatList.add(chat);
            chatContentListViewAdapter.notifyDataSetChanged();
            mTts.startSpeaking(info, mSynListener);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            Uri uri=data.getData();
            LogUtil.d("xzx","Uri=> "+uri.toString());
            ContentResolver contentResolver=this.getContentResolver();
            try {
                Bitmap bitmap= BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                chatContentListViewAdapter.setUserImgBitmap(bitmap);
                chatContentListViewAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                LogUtil.d("xzx","e=> "+e.toString());
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 语音听写初始化以及参数设置
     */
    void initSpeechRecognizer(){
        //1.创建SpeechRecognizer对象,第二个参数:本地听写时传InitListener
        mIat= SpeechRecognizer.createRecognizer(this, null);
        //2.设置听写参数,详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
    }

    /**
     * 语音合成初始化以及参数设置
     */
    void initSpeechSynthesizer(){
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        mTts= SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置,详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        //设置发音人(更多在线发音人,用户可参见 附录12.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量,范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //仅支持保存为 pcm 格式,如果不需要保存合成音频,注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
    }
    //{"sn":1,"ls":false,"bg":0,"ed":0,"ws":[  {"bg":0,"cw":[{"sc":0.00,"w":"广"}]}   ,{"bg":0,"cw":[{"sc":0.00,"w":"外"}]},{"bg":0,"cw":[{"sc":0.00,"w":"校长"}]},{"bg":0,"cw":[{"sc":0.00,"w":"是"}]},{"bg":0,"cw":[{"sc":0.00,"w":"谁"}]}]}
    String parseJsonToString(String jsonStr){
        try {
            String parseResultStr="";
            JSONObject jsonObject=new JSONObject(jsonStr);
            JSONArray wsJsonArray=jsonObject.getJSONArray("ws");
            JSONObject wsJsonObject;
            for(int i=0;i<wsJsonArray.length();i++){
                wsJsonObject =wsJsonArray.getJSONObject(i);
                parseResultStr=parseResultStr+wsJsonObject.getJSONArray("cw").getJSONObject(0).get("w");
            }
            return parseResultStr;
        } catch (JSONException e) {
            LogUtil.d("xzx","e=> "+e.toString());
            e.printStackTrace();
        }
        return jsonStr;
    }

    void wakeUpAndUnlock(){
        KeyguardManager km= (KeyguardManager) MainActivity.this.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.input_switch:
                if(isInputWithSay){
                    inputSwitchImageView.setImageResource(R.drawable.microphone_32);
                    pressToSayTextView.setVisibility(View.GONE);
                    userInputEditText.setVisibility(View.VISIBLE);
                    isInputWithSay=false;
                }else{
                    inputSwitchImageView.setImageResource(R.drawable.keyboard_32);
                    pressToSayTextView.setVisibility(View.VISIBLE);
                    userInputEditText.setVisibility(View.GONE);
                    isInputWithSay=true;
                }
                break;
            case R.id.send:
                String userInputStr= userInputEditText.getText().toString();

                new AnswerTask().execute(userInputStr);

                Chat chat=new Chat(true,userInputStr);
                chatContentListViewAdapter.chatList.add(chat);
                chatContentListViewAdapter.notifyDataSetChanged();
                userInputEditText.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    class AnswerTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                return Qa.getAnswer(params[0]);
            } catch (IOException e) {
                LogUtil.d("xzx","e=> "+e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String answer) {
            Chat chat=new Chat(false,answer);
            chatContentListViewAdapter.chatList.add(chat);
            chatContentListViewAdapter.notifyDataSetChanged();
            if(isAllowRobotToSay){
                mTts.startSpeaking(answer, mSynListener);
            }
        }
    }

    class ChatContentListViewAdapter extends BaseAdapter{
        List<Chat> chatList;
        Bitmap robotImgBitmap;
        Bitmap userImgBitmap;

        public ChatContentListViewAdapter(List<Chat> chatList) {
            this.chatList = chatList;
        }

        public void setRobotImgBitmap(Bitmap robotImgBitmap) {
            this.robotImgBitmap = robotImgBitmap;
        }

        public void setUserImgBitmap(Bitmap userImgBitmap) {
            this.userImgBitmap = userImgBitmap;
        }

        @Override
        public int getCount() {
            return chatList == null ? 0 : chatList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ChatViewHolder holder;
            if(null!=convertView){
                view=convertView;
                holder=(ChatViewHolder)view.getTag();
            }else{
                holder=new ChatViewHolder();
                view=View.inflate(MainActivity.this,R.layout.chat_item,null);
                holder.robotSayLayout=(LinearLayout)view.findViewById(R.id.robot_say_layout);
                holder.robotOutputTextView=(TextView)view.findViewById(R.id.robot_output);
                holder.robotImgView =(ImageView)view.findViewById(R.id.robot_img);
                holder.userSayLayout=(LinearLayout)view.findViewById(R.id.user_say_layout);
                holder.userInputTextView =(TextView)view.findViewById(R.id.user_input);
                holder.userImgView =(ImageView)view.findViewById(R.id.user_img);
                view.setTag(holder);
            }
            final Chat chat=chatList.get(position);
            if(chat.isUserInput){
                holder.robotSayLayout.setVisibility(View.GONE);
                holder.userSayLayout.setVisibility(View.VISIBLE);
                holder.userImgView.setImageBitmap(userImgBitmap);
                holder.userInputTextView.setText(chat.chatStr);
                holder.userImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 1);
                    }
                });
            }else{
                holder.robotSayLayout.setVisibility(View.VISIBLE);
                holder.userSayLayout.setVisibility(View.GONE);
                holder.robotImgView.setImageBitmap(robotImgBitmap);
                holder.robotOutputTextView.setText(chat.chatStr);
                holder.robotSayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTts.isSpeaking()) {
                            mTts.stopSpeaking();
                            return;
                        }
                        mTts.startSpeaking(chat.chatStr, mSynListener);
                    }
                });
                holder.robotImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(MainActivity.this,RobotSettingActivity.class);
                        startActivity(intent);
                    }
                });
            }
            return view;
        }
    }
    class ChatViewHolder {
        LinearLayout robotSayLayout;
        LinearLayout userSayLayout;
        TextView robotOutputTextView;
        TextView userInputTextView;
        ImageView robotImgView;
        ImageView userImgView;
    }
}
