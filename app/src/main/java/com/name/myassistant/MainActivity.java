package com.name.myassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.name.myassistant.m.Chat;
import com.name.myassistant.qoa.Qa;
import com.name.myassistant.shortMessage.SmsReceiver;
import com.name.myassistant.util.LocalDisplay;
import com.name.myassistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends TakePhotoActivity implements View.OnClickListener{

    //对话列表
    ListView chatContentListView;
    //对话列表对应的适配器
    ChatContentListViewAdapter chatContentListViewAdapter;
    //联系人及电话列表的layout
    FrameLayout contactLayout;
    //手电筒控制
    TextView closeFlashlightTextView;
    View closeFlashlightView;

    ProgressDialog progressDialog;

    //录音动画的View
    RelativeLayout volumeChangeLayout;
    View volumeTagView1;
    View volumeTagView2;
    View volumeTagView3;
    View volumeTagView4;
    View volumeTagView5;
    View volumeTagView6;
    //用户输入
    ImageView inputSwitchImageView;
    TextView pressToSayTextView;
    TextView sendTextView;
    EditText userInputEditText;

    //更改头像layout
    LinearLayout setImgLayout;
    TextView originalImgTextView;

    //手电筒状态（是否开着）
    boolean isFlashLightOn;
    //手电筒小按钮的纵坐标和横坐标
    int lastX;
    int lastY;
    Camera camera;
//    Animator mCurrentAnimator;

    //显示联系人列表的fragment和联系人的电话号码
    ContactFragment contactFragment;
    String phoneNum;
    //是否准备好发送短信
    boolean prepareToSendMessage;

    //语音识别出的字符串
    String recognizerStr;
    //是否允许机器人播报
    boolean isAllowRobotToSay;

    boolean isRobotImgChange;
    Uri imageUri;

    //SpeechRecognizer 语音听写对象
    SpeechRecognizer speechRecognizer;
    //SpeechSynthesizer 语音合成对象
    SpeechSynthesizer speechSynthesizer;

    //听写监听器
    RecognizerListener recognizerListener = new RecognizerListener() {
        //听写结果回调接口(返回Json格式结果,用户可参见附录12.1);
        //一般情况下会通过onResults接口多次返回结果,完整的识别内容是多次结果的累加;
        //关于解析Json的代码可参见MscDemo中JsonParser类;
        //isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            recognizerStr=recognizerStr + parseJsonToString(results.getResultString());
            if(isLast){
                setProgressBarDialogShow(false);
                userInputHandle(recognizerStr);
            }
        }

        //会话发生错误回调接口
        public void onError(SpeechError error) {
            LogUtil.d("xzx", "SpeechError=> " + error.toString());
            error.getPlainDescription(true);//获取错误码描述
            setProgressBarDialogShow(false);
            Toast.makeText(MainActivity.this,error.getErrorDescription(),Toast.LENGTH_LONG).show();
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
            setProgressBarDialogShow(false);
        }

        //扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

//    //合成监听器
//    SynthesizerListener mSynListener = new SynthesizerListener() {
//        //会话结束回调接口,没有错误时,error为null
//        public void onCompleted(SpeechError error) {
//        }
//
//        @Override
//        public void onEvent(int i, int i1, int i2, Bundle bundle) {
//
//        }
//
//        //缓冲进度回调
//        //percent为缓冲进度0~100,beginPos为缓冲音频在文本中开始位置,endPos表示缓冲音频在文本中结束位置,info为附加信息。
//        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
//        }
//
//        //开始播放
//        public void onSpeakBegin() {
//        }
//
//        //暂停播放
//        public void onSpeakPaused() {
//        }
//
//        //播放进度回调
//        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
//        public void onSpeakProgress(int percent, int beginPos, int endPos) {
//        }
//
//        //恢复播放回调接口
//        public void onSpeakResumed() {
//        }
//        //会话事件回调接口
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        chatContentListView = (ListView) findViewById(R.id.chat_content);
        List<Chat> chatList = new ArrayList<>();
        chatContentListViewAdapter = new ChatContentListViewAdapter(chatList);
        chatContentListView.setAdapter(chatContentListViewAdapter);

        contactLayout=(FrameLayout)findViewById(R.id.contact_layout);

        closeFlashlightTextView = (TextView) findViewById(R.id.close_flashlight);
        closeFlashlightView=findViewById(R.id.close_flashlight_view);

        volumeChangeLayout=(RelativeLayout)findViewById(R.id.volume_change_layout);
        volumeTagView1=findViewById(R.id.volume_tag_1);
        volumeTagView2=findViewById(R.id.volume_tag_2);
        volumeTagView3=findViewById(R.id.volume_tag_3);
        volumeTagView4=findViewById(R.id.volume_tag_4);
        volumeTagView5=findViewById(R.id.volume_tag_5);
        volumeTagView6=findViewById(R.id.volume_tag_6);

        inputSwitchImageView = (ImageView) findViewById(R.id.input_switch);
        pressToSayTextView = (TextView) findViewById(R.id.press_to_say);
        userInputEditText = (EditText) findViewById(R.id.question_input);
        sendTextView = (TextView) findViewById(R.id.send);

        setImgLayout=(LinearLayout)findViewById(R.id.set_img_layout);
        TextView takePhotoTextView=(TextView)findViewById(R.id.take_photo);
        TextView selectPictureTextView=(TextView)findViewById(R.id.select_picture);
        TextView noImgTextView=(TextView)findViewById(R.id.no_img);
        originalImgTextView=(TextView)findViewById(R.id.original_img);
        originalImgTextView.setOnClickListener(this);

        contactLayout.setOnClickListener(this);

        closeFlashlightTextView.setOnClickListener(this);
        closeFlashlightView.setOnClickListener(this);
        closeFlashlightView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

                        int top = v.getTop() + dy;
                        int bottom = v.getBottom() + dy;
                        int left = v.getLeft() + dx;
                        int right = v.getRight() + dx;

                        if (top < 0) {
                            top = 0;
                            bottom = v.getHeight();
                        }
                        if (bottom > LocalDisplay.SCREEN_HEIGHT_PIXELS) {
                            bottom = LocalDisplay.SCREEN_HEIGHT_PIXELS;
                            top = bottom - v.getHeight();
                        }
                        if (left < 0) {
                            left = 0;
                            right = v.getWidth();
                        }
                        if (right > LocalDisplay.SCREEN_WIDTH_PIXELS) {
                            right = LocalDisplay.SCREEN_WIDTH_PIXELS;
                            left = right - v.getWidth();
                        }
                        v.layout(left, top, right, bottom);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });

        //初始化，创建语音配置对象
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.app_id));

        initSpeechRecognizer();
        initSpeechSynthesizer();

        inputSwitchImageView.setOnClickListener(this);
        sendTextView.setOnClickListener(this);

        pressToSayTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        //开始听写
                        volumeChangeLayout.setVisibility(View.VISIBLE);
                        recognizerStr = "";
                        pressToSayTextView.setText(getString(R.string.loosen_to_end));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_light_gray_solid);
                        speechRecognizer.startListening(recognizerListener);
                        return true;
                    case MotionEvent.ACTION_UP:
                        volumeChangeLayout.setVisibility(View.GONE);
                        pressToSayTextView.setText(getString(R.string.press_and_say));
                        pressToSayTextView.setBackgroundResource(R.drawable.oval_gray);
                        if (speechRecognizer.isListening()) {
                            speechRecognizer.stopListening();
                        }
                        setProgressBarDialogShow(true);
                        return true;
                    default:
                        return false;
                }
            }
        });

        takePhotoTextView.setOnClickListener(this);
        selectPictureTextView.setOnClickListener(this);
        noImgTextView.setOnClickListener(this);

        String robotOutputStr=getIntent().getStringExtra("robotOutputStr");
        if(!TextUtils.isEmpty(robotOutputStr)){
            robotOutputHandle(robotOutputStr);
        }

        SmsReceiver.setmSmsListener(new SmsReceiver.SmsListener() {
            @Override
            public void onReceive(String msg) {
                robotOutputHandle(msg);
            }
        });

        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        imageUri = Uri.fromFile(file);
        userInputEditText.setText("广外校长是谁");

        shakeToClean(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean userHasImg=GlobalVariable.getInstance().isUSER_HAS_IMG();
        if(userHasImg){
            File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.user_img_tag) + ".jpg");
            imageUri = Uri.fromFile(file);
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 5;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), option);
            chatContentListViewAdapter.setUserImgBitmap(bitmap);
        }else{
            chatContentListViewAdapter.setUserImgBitmap(null);
        }


        int robotImgStatus=GlobalVariable.getInstance().getRobotImgStatus();
        if(robotImgStatus==GlobalVariable.NEW_IMG){
            File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.robot_img_tag) + ".jpg");
            imageUri = Uri.fromFile(file);
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 5;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), option);
            chatContentListViewAdapter.setRobotImgBitmap(bitmap);
        }else if(robotImgStatus==GlobalVariable.ORIGINAL_IMG){
            Bitmap userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot_img);
            chatContentListViewAdapter.setRobotImgBitmap(userBitmap);
        }else{
            chatContentListViewAdapter.setRobotImgBitmap(null);
        }
        chatContentListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        //手电筒的控制
        if(isFlashLightOn){
            if(closeFlashlightTextView.getVisibility()==View.VISIBLE){
                zoomOutFlashLightControl();
                return;
            }else{
                controlFlashlight(false);
            }
        }

        LogUtil.d("xzx","getSupportFragmentManager().getBackStackEntryCount()=> "+getSupportFragmentManager().getBackStackEntryCount());
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
            contactLayout.setVisibility(View.INVISIBLE);
            return;
        }
        if(setImgLayout.getVisibility()==View.VISIBLE){
            setImgLayout.setVisibility(View.GONE);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        SmsReceiver.setmSmsListener(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }else{
            Intent intent=new Intent(MainActivity.this,HelpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_flashlight:
                controlFlashlight(false);
//                closeFlashlightTextView.setVisibility(View.GONE);
                break;
            case R.id.close_flashlight_view:
                controlFlashlight(false);
//                closeFlashlightView.setVisibility(View.GONE);
                break;
            case R.id.input_switch:
                if(pressToSayTextView.getVisibility()==View.VISIBLE){
                    inputSwitchImageView.setImageResource(R.drawable.microphone_32);
                    pressToSayTextView.setVisibility(View.GONE);
                    userInputEditText.setVisibility(View.VISIBLE);
                    sendTextView.setVisibility(View.VISIBLE);
                }else{
                    inputSwitchImageView.setImageResource(R.drawable.keyboard_32);
                    pressToSayTextView.setVisibility(View.VISIBLE);
                    userInputEditText.setVisibility(View.GONE);
                    sendTextView.setVisibility(View.GONE);
                }
                break;
            case R.id.send:
                String userInputStr = userInputEditText.getText().toString();
                userInputEditText.setText("");
                userInputHandle(userInputStr);
                break;
            case R.id.contact_layout:
                getSupportFragmentManager().popBackStack();
                contactLayout.setVisibility(View.INVISIBLE);
                prepareToSendMessage=false;
                break;
            case R.id.take_photo:
                this.getTakePhoto().picTakeCrop(imageUri);
                break;
            case R.id.select_picture:
                getTakePhoto().picSelectCrop(imageUri);
                break;
            case R.id.no_img:
                if(isRobotImgChange){
                    GlobalVariable.getInstance().setRobotImgStatus(GlobalVariable.NO_IMG);
//                    GlobalVariable.getInstance().setROBOT_HAS_IMG(false);
                }else{
                    GlobalVariable.getInstance().setUSER_HAS_IMG(false);
                }
                GlobalVariable.save(MainActivity.this);
                setImgLayout.setVisibility(View.GONE);
                onResume();
                break;
            case R.id.original_img:
                GlobalVariable.getInstance().setRobotImgStatus(GlobalVariable.ORIGINAL_IMG);
                GlobalVariable.save(MainActivity.this);
                setImgLayout.setVisibility(View.GONE);
                onResume();
                break;
            default:
                break;
        }
    }

    /**
     * 用户输入处理
     * @param userInput:用户输入文本
     */
    void userInputHandle(String userInput){
        //将用户的输入输出到屏幕上
        Chat chat = new Chat(true, userInput);
        chatContentListViewAdapter.chatList.add(chat);
        chatContentListViewAdapter.notifyDataSetChanged();

        chatContentListView.smoothScrollToPosition(chatContentListViewAdapter.getCount());

        //查看帮助
        if(userInput.contains(getString(R.string.help))){
            Intent intent=new Intent(MainActivity.this,HelpActivity.class);
            startActivity(intent);
            return;
        }

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
            return;
        }

        String searchTag=getString(R.string.search);
        if(userInput.contains(searchTag)){
            String searchMsg=userInput.replace(searchTag,"");
            try {
                String link="http://m.baidu.com/s?from=1086k&word="+ URLEncoder.encode(searchMsg, "utf-8");

                Intent intent = new Intent(MainActivity.this, LookOtherInfoWebActivity.class);
                intent.putExtra("link", link);
                startActivity(intent);
            } catch (UnsupportedEncodingException e) {
                LogUtil.e("xzx","e=> "+e.toString());
                e.printStackTrace();
            }
            return;
        }

        new AnswerTask().execute(userInput);
        setProgressBarDialogShow(true);
    }

    /**
     * 机器人输出处理
     * @param outputStr：机器人输出文本
     */
    void robotOutputHandle(String outputStr){
        boolean isRobotAnswer=true;
        //收到新短信时的处理
        String receiveSMSTag=getString(R.string.short_message_tip);
        if(outputStr.contains(receiveSMSTag)){
            int index=outputStr.indexOf(receiveSMSTag);
            LogUtil.d("xzx","index=> "+index);
            String phoneNum=outputStr.substring(0, index);
            LogUtil.d("xzx","phoneNum=> "+phoneNum);
            String contactName=getContactNameWithPhoneNum(phoneNum);
            LogUtil.d("xzx","contactName=> "+contactName);
            outputStr=outputStr.replace(phoneNum, contactName);
            isRobotAnswer=false;
            wakeUpAndUnlock();
        }
        if(outputStr.equals(getString(R.string.say_something))){
            isRobotAnswer=false;
        }
//        if(){
//            wakeUpAndUnlock();
//        }
        Chat chat = new Chat(false, outputStr);
        chat.setIsRobotAnswer(isRobotAnswer);
        chatContentListViewAdapter.chatList.add(chat);
        chatContentListViewAdapter.notifyDataSetChanged();
        chatContentListView.smoothScrollToPosition(chatContentListViewAdapter.getCount());

        if(GlobalVariable.getInstance().isAllowToSay()){
            speechSynthesizer.startSpeaking(outputStr, null);
        }
    }

    /**
     * 手电筒控制
     * @param open：开或者关
     */
    void controlFlashlight(boolean open) {
        LogUtil.d("xzx");
        isFlashLightOn=open;
        if (open) {
            closeFlashlightTextView.setVisibility(View.VISIBLE);
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
//                userInputEditText.setText("打开手电筒");
                closeFlashlightTextView.setVisibility(View.GONE);
                closeFlashlightView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 手电筒控制按钮由大变小的动画
     */
    void zoomOutFlashLightControl(){
        LogUtil.d("xzx");
//        if (mCurrentAnimator != null) {
//            mCurrentAnimator.cancel();
//        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        closeFlashlightView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        closeFlashlightView.setVisibility(View.VISIBLE);
        closeFlashlightView.setAlpha(0f);

//        closeFlashlightTextView.setPivotX(0f);
//        closeFlashlightTextView.setPivotY(0f);
        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(closeFlashlightTextView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(closeFlashlightTextView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(closeFlashlightTextView,
                                View.SCALE_X, startScale))
                .with(ObjectAnimator
                        .ofFloat(closeFlashlightTextView,
                                View.SCALE_Y, startScale));
        set.setDuration(700);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtil.d("xzx");

                closeFlashlightView.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .setListener(null);
                closeFlashlightTextView.setVisibility(View.GONE);
//                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                LogUtil.d("xzx");
                closeFlashlightView.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .setListener(null);
                closeFlashlightTextView.setVisibility(View.GONE);
//                mCurrentAnimator = null;
            }
        });
        set.start();
//        mCurrentAnimator = set;
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
        contactLayout.setVisibility(View.VISIBLE);
//        if (contactFragment == null) {
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            contactFragment = new ContactFragment();
            contactFragment.setmSearchString(contactName);
            fragmentTransaction.add(R.id.contact_layout, contactFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
//        }
//        else {
//            // TODO: 16/4/2 Fragment ContactFragment{41aa8008} not attached to Activity
////            if(contactFragment.a){}
//            contactFragment = new ContactFragment();
//            contactFragment.setmSearchString(contactName);
//            contactFragment.getLoaderManager().restartLoader(0, null, contactFragment);
//        }
    }

    void sendShortMessage(String phoneNum,String message){
        android.telephony.SmsManager smsManager=android.telephony.SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNum, null, message, null, null);
        Toast.makeText(this,getString(R.string.send_sms_successfully),Toast.LENGTH_LONG).show();
        onBackPressed();
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
            progressDialog=ProgressDialog.show(this,null,getString(R.string.please_wait),false,true);
        }else{
            if(progressDialog!=null){
                progressDialog.cancel();
            }
        }
    }

    void deleteText(Chat chat){
        chatContentListViewAdapter.chatList.remove(chat);
        chatContentListViewAdapter.notifyDataSetChanged();
        Toast.makeText(this,getString(R.string.delete_successfully),Toast.LENGTH_LONG).show();
    }

    void copyText(String text){
        ClipboardManager clipboardManager=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(text);
        Toast.makeText(this,getString(R.string.copy_successfully),Toast.LENGTH_LONG).show();
    }

    //摇一摇清除记录
    void shakeToClean(Context context){
        SensorManager sensorManager=(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] values=event.values;
                float x=values[0];
                float y=values[1];
                float z=values[2];

                int value=15;
                if(Math.abs(x)>=value||Math.abs(y)>=value){
                    chatContentListViewAdapter.chatList.clear();
                    chatContentListViewAdapter.notifyDataSetChanged();
                    LogUtil.d("xzx","Math.abs(x)=> "+Math.abs(x)+" Math.abs(y)=> "+Math.abs(y)+" Math.abs(z)=> "+Math.abs(z));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        },sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    void getTime(){
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int hour=calendar.get(Calendar.HOUR);
        int minute=calendar.get(Calendar.MINUTE);
        int second=calendar.get(Calendar.SECOND);
        LogUtil.d("xzx", " " + year + " " + month + " " + day + " " + hour + " " + minute + " " + second);
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
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        wl.release();
    }

    /**
     * 语音听写初始化以及参数设置
     */
    void initSpeechRecognizer() {
        //1.创建SpeechRecognizer对象,第二个参数:本地听写时传InitListener
        speechRecognizer = SpeechRecognizer.createRecognizer(this, null);
        //2.设置听写参数,详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        speechRecognizer.setParameter(SpeechConstant.DOMAIN, "iat");
        speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin ");
    }

    /**
     * 语音合成初始化以及参数设置
     */
    void initSpeechSynthesizer() {
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置,详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        //设置发音人(更多在线发音人,用户可参见 附录12.2
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");//设置语速
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "80");//设置音量,范围 0~100
        speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //仅支持保存为 pcm 格式,如果不需要保存合成音频,注释该行代码
        speechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
    }

    //{"sn":1,"ls":false,"bg":0,"ed":0,"ws":[  {"bg":0,"cw":[{"sc":0.00,"w":"广"}]}   ,{"bg":0,"cw":[{"sc":0.00,"w":"外"}]},{"bg":0,"cw":[{"sc":0.00,"w":"校长"}]},{"bg":0,"cw":[{"sc":0.00,"w":"是"}]},{"bg":0,"cw":[{"sc":0.00,"w":"谁"}]}]}
    String parseJsonToString(String jsonStr) {
        try {
            String parseResultStr = "";
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray wsJsonArray = jsonObject.getJSONArray("ws");
//            recognizeFinish=jsonObject.getBoolean("ls");
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

    @Override
    public void takeSuccess(Uri uri) {
        LogUtil.d("xzx","uri=> "+uri.getPath()+" => "+uri.toString());

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 5;
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath(), option);
        if(isRobotImgChange){
            GlobalVariable.getInstance().setRobotImgStatus(GlobalVariable.NEW_IMG);
//            GlobalVariable.getInstance().setROBOT_HAS_IMG(true);
            chatContentListViewAdapter.setRobotImgBitmap(bitmap);
        }else{
            GlobalVariable.getInstance().setUSER_HAS_IMG(true);
            chatContentListViewAdapter.setUserImgBitmap(bitmap);
        }
        GlobalVariable.save(this);
        chatContentListViewAdapter.notifyDataSetChanged();
        setImgLayout.setVisibility(View.GONE);
    }

    @Override
    public void takeFail(String msg) {

    }

    @Override
    public void takeCancel() {

    }

    class AnswerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return Qa.getAnswer(params[0]);
            } catch (IOException e) {
                LogUtil.d("xzx", "e=> " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String answer) {
            setProgressBarDialogShow(false);
            robotOutputHandle(answer);
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
            final ChatViewHolder holder;
            if (null != convertView) {
                view = convertView;
                holder = (ChatViewHolder) view.getTag();
            } else {
                holder = new ChatViewHolder();
                view = View.inflate(MainActivity.this, R.layout.chat_item, null);
                holder.robotSayLayout = (RelativeLayout) view.findViewById(R.id.robot_say_layout);
                holder.robotImgView = (ImageView) view.findViewById(R.id.robot_img);
                holder.robotOutputTextView = (TextView) view.findViewById(R.id.robot_output);
                holder.longClickLayout=(LinearLayout)view.findViewById(R.id.long_click_layout);
                holder.copyTextView=(TextView)view.findViewById(R.id.copy);
                holder.deleteTextView=(TextView)view.findViewById(R.id.delete);
                holder.moreTextView=(TextView)view.findViewById(R.id.look_in_bai_du);
                holder.lookMoreLine=view.findViewById(R.id.look_more_line);

                holder.userSayLayout = (RelativeLayout) view.findViewById(R.id.user_say_layout);
                holder.userImgView = (ImageView) view.findViewById(R.id.user_img);
                holder.userInputTextView = (TextView) view.findViewById(R.id.user_input);
                holder.userInputLongClickLayout=(LinearLayout)view.findViewById(R.id.user_input_long_click_layout);
                holder.userCopyTextView=(TextView)view.findViewById(R.id.copy_user_input);
                holder.userDeleteTextView=(TextView)view.findViewById(R.id.delete_user_input);
                view.setTag(holder);
            }

            holder.longClickLayout.setVisibility(View.GONE);
            holder.moreTextView.setVisibility(View.GONE);
            holder.userInputLongClickLayout.setVisibility(View.GONE);

            final Chat chat = chatList.get(position);

            if (chat.isUserInput) {
                holder.robotSayLayout.setVisibility(View.GONE);
                holder.userSayLayout.setVisibility(View.VISIBLE);
                holder.userImgView.setImageBitmap(userImgBitmap);
                holder.userInputTextView.setText(chat.chatStr);

                holder.userImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        isRobotImgChange=false;
                        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.user_img_tag) + ".jpg");
                        imageUri = Uri.fromFile(file);
                        setImgLayout.setVisibility(View.VISIBLE);
                        originalImgTextView.setVisibility(View.GONE);
//                        Intent intent = new Intent();
//                        intent.setType("image/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        startActivityForResult(intent, 1);
                    }
                });
                holder.userInputTextView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        holder.userInputLongClickLayout.setVisibility(View.VISIBLE);
                        return false;
                    }
                });
                holder.userCopyTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyText(chat.chatStr);
                        holder.userInputLongClickLayout.setVisibility(View.INVISIBLE);
                    }
                });
                holder.userDeleteTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteText(chat);
                        holder.userInputLongClickLayout.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                holder.robotSayLayout.setVisibility(View.VISIBLE);
                holder.userSayLayout.setVisibility(View.GONE);
                holder.robotImgView.setImageBitmap(robotImgBitmap);
                holder.robotOutputTextView.setText(chat.chatStr);

                holder.robotImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.robot_img_tag) + ".jpg");
                        imageUri = Uri.fromFile(file);
                        isRobotImgChange=true;
                        setImgLayout.setVisibility(View.VISIBLE);
                        originalImgTextView.setVisibility(View.VISIBLE);
                    }
                });
                holder.robotOutputTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (speechSynthesizer.isSpeaking()) {
                            speechSynthesizer.stopSpeaking();
                            return;
                        }
                        if(GlobalVariable.getInstance().isAllowToSay()){
                            speechSynthesizer.startSpeaking(chat.chatStr, null);
                        }

                    }
                });
                holder.robotOutputTextView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        LogUtil.d("xzx");
                        holder.longClickLayout.setVisibility(View.VISIBLE);
                        if (chat.isRobotAnswer()) {
                            holder.moreTextView.setVisibility(View.VISIBLE);
                            holder.lookMoreLine.setVisibility(View.VISIBLE);
                        } else {
                            holder.moreTextView.setVisibility(View.GONE);
                            holder.lookMoreLine.setVisibility(View.GONE);
                        }
                        return false;
                    }
                });
                holder.moreTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String link = GlobalVariable.getInstance().getLink();
                        Intent intent = new Intent(MainActivity.this, LookOtherInfoWebActivity.class);
                        intent.putExtra("link", link);
                        startActivity(intent);
                    }
                });
                holder.copyTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyText(holder.robotOutputTextView.getText().toString());
                        holder.longClickLayout.setVisibility(View.INVISIBLE);
                    }
                });

                holder.deleteTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteText(chat);
                        holder.longClickLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }
            return view;
        }
    }

    class ChatViewHolder {
        RelativeLayout robotSayLayout;
        RelativeLayout userSayLayout;
        LinearLayout longClickLayout;
        LinearLayout userInputLongClickLayout;

        TextView moreTextView;
        View lookMoreLine;
        TextView copyTextView;
        TextView deleteTextView;

        TextView userCopyTextView;
        TextView userDeleteTextView;

        TextView robotOutputTextView;
        TextView userInputTextView;
        ImageView robotImgView;
        ImageView userImgView;
    }
}
