package com.name.myassistant;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    ImageView inputSwitchImageView;
    TextView pressToSayTextView;
    TextView closeFlashlightTextView;
    TextView sendTextView;
    EditText userInputEditText;

    ListView chatContentListView;

    FrameLayout contactLayout;
    ProgressDialog progressDialog;

    RelativeLayout volumeChangeLayout;
    View volumeTagView1;
    View volumeTagView2;
    View volumeTagView3;
    View volumeTagView4;
    View volumeTagView5;
    View volumeTagView6;


    ChatContentListViewAdapter chatContentListViewAdapter;

    boolean isInputWithSay = true;
    boolean isAllowRobotToSay;

    //SpeechRecognizer 语音听写对象
    SpeechRecognizer mIat;
    //SpeechSynthesizer 语音合成对象
    SpeechSynthesizer mTts;

    PowerManager.WakeLock wl;

    Camera camera;

    ContactFragment contactFragment;

    String phoneNum;

//    boolean prepareToCallPhone;
    boolean prepareToSendMessage;

    boolean recognizeFinish;
    String recognizerStr;

    //听写监听器
    RecognizerListener mRecoListener = new RecognizerListener() {
        //听写结果回调接口(返回Json格式结果,用户可参见附录12.1);
        //一般情况下会通过onResults接口多次返回结果,完整的识别内容是多次结果的累加;
        //关于解析Json的代码可参见MscDemo中JsonParser类;
        //isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            recognizerStr=recognizerStr + parseJsonToString(results.getResultString());
            if(recognizeFinish){
                setProgressBarDialogShow(false);
                userInputHandle(recognizerStr);
                recognizeFinish=false;
            }
        }

        //会话发生错误回调接口
        public void onError(SpeechError error) {
            LogUtil.d("xzx", "SpeechError=> " + error.toString());
            error.getPlainDescription(true);//获取错误码描述
        }

        //音量值0~30
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            changeVolumeView(i);
        }

        //开始录音
        public void onBeginOfSpeech() {
        }

        //结束录音
        public void onEndOfSpeech() {
        }

        //扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    //合成监听器
    SynthesizerListener mSynListener = new SynthesizerListener() {
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        chatContentListView = (ListView) findViewById(R.id.chat_content);
        inputSwitchImageView = (ImageView) findViewById(R.id.input_switch);
        pressToSayTextView = (TextView) findViewById(R.id.press_to_say);
        userInputEditText = (EditText) findViewById(R.id.question_input);
        sendTextView = (TextView) findViewById(R.id.send);

        closeFlashlightTextView = (TextView) findViewById(R.id.close_flashlight);
        contactLayout=(FrameLayout)findViewById(R.id.contact_layout);

        volumeChangeLayout=(RelativeLayout)findViewById(R.id.volume_change_layout);
        volumeTagView1=findViewById(R.id.volume_tag_1);
        volumeTagView2=findViewById(R.id.volume_tag_2);
        volumeTagView3=findViewById(R.id.volume_tag_3);
        volumeTagView4=findViewById(R.id.volume_tag_4);
        volumeTagView5=findViewById(R.id.volume_tag_5);
        volumeTagView6=findViewById(R.id.volume_tag_6);

        contactLayout.setOnClickListener(this);
        List<Chat> chatList = new ArrayList<>();
        chatContentListViewAdapter = new ChatContentListViewAdapter(chatList);
        Bitmap userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_img_48);
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
                        volumeChangeLayout.setVisibility(View.VISIBLE);
                        recognizerStr="";
                        pressToSayTextView.setText(getString(R.string.loosen_to_end));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_light_gray_solid);
                        mIat.startListening(mRecoListener);
                        return true;
                    case MotionEvent.ACTION_UP:
                        volumeChangeLayout.setVisibility(View.GONE);
                        pressToSayTextView.setText(getString(R.string.press_and_say));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_gray);
                        if (mIat.isListening()) {
                            mIat.stopListening();
                        }
                        setProgressBarDialogShow(true);
                        return true;
                    default:
                        return false;
                }
            }
        });

        closeFlashlightTextView.setOnClickListener(this);
        sendTextView.setOnClickListener(this);

        //初始化，创建语音配置对象
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.app_id));

        initSpeechRecognizer();
        initSpeechSynthesizer();

        String phoneNum = getIntent().getStringExtra("phoneNum");
        String contactName = "";
        if (!TextUtils.isEmpty(phoneNum)) {
            contactName = contactName + getContactNameWithPhoneNum(phoneNum);
        }

        String info = getIntent().getStringExtra("info");
        if (info != null) {
            info = contactName + getString(R.string.short_message_tip) + info;
            wakeUpAndUnlock();
            Chat chat = new Chat(false, info);
            chatContentListViewAdapter.chatList.add(chat);
            chatContentListViewAdapter.notifyDataSetChanged();
            mTts.startSpeaking(info, mSynListener);
        }
        userInputEditText.setText("广外校长是谁");
    }

    @Override
    public void onBackPressed() {
        LogUtil.d("xzx","getSupportFragmentManager().getBackStackEntryCount()=> "+getSupportFragmentManager().getBackStackEntryCount());
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            LogUtil.d("xzx", "Uri=> " + uri.toString());
            ContentResolver contentResolver = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                chatContentListViewAdapter.setUserImgBitmap(bitmap);
                chatContentListViewAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                LogUtil.d("xzx", "e=> " + e.toString());
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setTitle(getString(R.string.show_look_more));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, RobotSettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 语音听写初始化以及参数设置
     */
    void initSpeechRecognizer() {
        //1.创建SpeechRecognizer对象,第二个参数:本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(this, null);
        //2.设置听写参数,详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
    }

    /**
     * 语音合成初始化以及参数设置
     */
    void initSpeechSynthesizer() {
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
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
    String parseJsonToString(String jsonStr) {
        try {
            String parseResultStr = "";
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray wsJsonArray = jsonObject.getJSONArray("ws");
            recognizeFinish=jsonObject.getBoolean("ls");
            JSONObject wsJsonObject;
            for (int i = 0; i < wsJsonArray.length(); i++) {
                wsJsonObject = wsJsonArray.getJSONObject(i);
                parseResultStr = parseResultStr + wsJsonObject.getJSONArray("cw").getJSONObject(0).get("w");
            }
            return parseResultStr;
        } catch (JSONException e) {
            LogUtil.d("xzx", "e=> " + e.toString());
            e.printStackTrace();
        }
        return jsonStr;
    }

    void wakeUpAndUnlock() {
        LogUtil.d("xzx", "wakeUpAndUnlock");
        KeyguardManager km = (KeyguardManager) MainActivity.this.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            return;
        }
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        wl.release();
    }

    void controlFlashlight(boolean open) {
        LogUtil.d("xzx");
        if (open) {
            LogUtil.d("xzx");
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        } else {
            LogUtil.d("xzx");
            if (camera != null) {
                camera.stopPreview();
                camera.release();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_flashlight:
                controlFlashlight(false);
                closeFlashlightTextView.setVisibility(View.GONE);
                break;
            case R.id.input_switch:
                if (isInputWithSay) {
                    inputSwitchImageView.setImageResource(R.drawable.microphone_32);
                    pressToSayTextView.setVisibility(View.GONE);
                    userInputEditText.setVisibility(View.VISIBLE);
                    sendTextView.setVisibility(View.VISIBLE);
                    isInputWithSay = false;
                } else {
                    inputSwitchImageView.setImageResource(R.drawable.keyboard_32);
                    pressToSayTextView.setVisibility(View.VISIBLE);
                    userInputEditText.setVisibility(View.GONE);
                    sendTextView.setVisibility(View.GONE);
                    isInputWithSay = true;
                }
                break;
            case R.id.send:
                String userInputStr = userInputEditText.getText().toString();
                userInputEditText.setText("");
                userInputHandle(userInputStr);
                break;
            case R.id.contact_layout:
                getSupportFragmentManager().popBackStack();
                contactLayout.setVisibility(View.GONE);
                prepareToSendMessage=false;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    class AnswerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return Qa.getAnswer(params[0]);
            } catch (IOException e) {
                LogUtil.d("xzx", "e=> " + e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String answer) {
            Chat chat = new Chat(false, answer);
            chat.setIsRobotAnswer(true);
            chatContentListViewAdapter.chatList.add(chat);
            chatContentListViewAdapter.notifyDataSetChanged();
            if (isAllowRobotToSay) {
                mTts.startSpeaking(answer, mSynListener);
            }
            setProgressBarDialogShow(false);
        }
    }


    class ChatContentListViewAdapter extends BaseAdapter {
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
            if (null != convertView) {
                view = convertView;
                holder = (ChatViewHolder) view.getTag();
            } else {
                holder = new ChatViewHolder();
                view = View.inflate(MainActivity.this, R.layout.chat_item, null);
                holder.robotSayLayout = (LinearLayout) view.findViewById(R.id.robot_say_layout);
                holder.robotOutputTextView = (TextView) view.findViewById(R.id.robot_output);
                holder.lookInBaiDuTextView =(TextView)view.findViewById(R.id.look_in_bai_du);
                holder.robotImgView = (ImageView) view.findViewById(R.id.robot_img);
                holder.userSayLayout = (LinearLayout) view.findViewById(R.id.user_say_layout);
                holder.userInputTextView = (TextView) view.findViewById(R.id.user_input);
                holder.userImgView = (ImageView) view.findViewById(R.id.user_img);
                view.setTag(holder);
            }
            final Chat chat = chatList.get(position);
            if (chat.isUserInput) {
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
            } else {
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
                        Intent intent = new Intent(MainActivity.this, RobotSettingActivity.class);
                        startActivity(intent);
                    }
                });
                if(chat.isRobotAnswer()){
                    holder.lookInBaiDuTextView.setVisibility(View.VISIBLE);
                    holder.lookInBaiDuTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String link = GlobalVariable.getInstance().getLink();
                            Intent intent = new Intent(MainActivity.this, LookOtherInfoWebActivity.class);
                            intent.putExtra("link", link);
                            startActivity(intent);
                        }
                    });
                }else{
                    holder.lookInBaiDuTextView.setVisibility(View.GONE);
                    holder.lookInBaiDuTextView.setOnClickListener(null);
                }
            }
            return view;
        }
    }

    class ChatViewHolder {
        LinearLayout robotSayLayout;
        LinearLayout userSayLayout;
        TextView robotOutputTextView;
        TextView lookInBaiDuTextView;
        TextView userInputTextView;
        ImageView robotImgView;
        ImageView userImgView;
    }

    String getContactNameWithPhoneNum(String phoneNum) {
        String[] PHONE_PROJECTION = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME};
        ContentResolver resolver = getContentResolver();
        Uri lookUpUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
        Cursor cursor = resolver.query(lookUpUri, PHONE_PROJECTION, null, null, null);
        String contactName = null;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                contactName = cursor.getString(1);
            }
            cursor.close();
        } else {
            contactName = getString(R.string.num) + phoneNum;
        }
        return contactName;
    }

    void getPhoneNumWithContactName(String contactName) {
        if (contactFragment == null) {
            contactLayout.setVisibility(View.VISIBLE);
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            contactFragment = new ContactFragment();
            contactFragment.setmSearchString(contactName);
            fragmentTransaction.add(R.id.contact_layout, contactFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            contactFragment.setmSearchString(contactName);
            contactFragment.getLoaderManager().restartLoader(0, null, contactFragment);
        }
    }

    void sendShortMessage(String phoneNum,String message){
        android.telephony.SmsManager smsManager=android.telephony.SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNum, null, message, null, null);
    }

    void userInputHandle(String userInput){
        //将用户的输入输出到屏幕上
        Chat chat = new Chat(true, userInput);
        chatContentListViewAdapter.chatList.add(chat);
        chatContentListViewAdapter.notifyDataSetChanged();

        chatContentListView.smoothScrollToPosition(chatContentListViewAdapter.getCount());

        //打电话
        String callTag = getString(R.string.call_somebody);
        if (userInput.contains(callTag)) {
            String contactName = userInput.replace(callTag, "");
            getPhoneNumWithContactName(contactName);
            return;
        }

        //发短信
        if (prepareToSendMessage) {
            sendShortMessage(phoneNum, userInput);
            prepareToSendMessage = false;
            return;
        }
        String smsTag = getString(R.string.send_message_to);
        if (userInput.contains(smsTag)) {
            String contactName = userInput.replace(smsTag, "");
            prepareToSendMessage = true;
            getPhoneNumWithContactName(contactName);
            return;
        }

        //打开手电筒
        if (userInput.contains(getString(R.string.open_flashlight))) {
            controlFlashlight(true);
            closeFlashlightTextView.setVisibility(View.VISIBLE);
            return;
        }

        new AnswerTask().execute(userInput);
        setProgressBarDialogShow(true);
    }


    void changeVolumeView(int i){
        volumeTagView1.setVisibility(View.VISIBLE);
        volumeTagView2.setVisibility(View.VISIBLE);
        volumeTagView3.setVisibility(View.VISIBLE);
        volumeTagView4.setVisibility(View.VISIBLE);
        volumeTagView5.setVisibility(View.VISIBLE);
        volumeTagView6.setVisibility(View.VISIBLE);
        switch (i/5){
            case 0:
                volumeTagView1.setVisibility(View.INVISIBLE);
                volumeTagView2.setVisibility(View.INVISIBLE);
                volumeTagView3.setVisibility(View.INVISIBLE);
                volumeTagView4.setVisibility(View.INVISIBLE);
                volumeTagView5.setVisibility(View.INVISIBLE);
                break;
            case 1:
                volumeTagView1.setVisibility(View.INVISIBLE);
                volumeTagView2.setVisibility(View.INVISIBLE);
                volumeTagView3.setVisibility(View.INVISIBLE);
                volumeTagView4.setVisibility(View.INVISIBLE);
                break;
            case 2:
                volumeTagView1.setVisibility(View.INVISIBLE);
                volumeTagView2.setVisibility(View.INVISIBLE);
                volumeTagView3.setVisibility(View.INVISIBLE);
                break;
            case 3:
                volumeTagView1.setVisibility(View.INVISIBLE);
                volumeTagView2.setVisibility(View.INVISIBLE);
                break;
            case 4:
                volumeTagView1.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    void setProgressBarDialogShow(boolean isShow){

        if(isShow){
            progressDialog=ProgressDialog.show(this,null,getString(R.string.please_wait),false,false);
        }else{
            if(progressDialog!=null){
                progressDialog.cancel();
            }
        }
    }
}
