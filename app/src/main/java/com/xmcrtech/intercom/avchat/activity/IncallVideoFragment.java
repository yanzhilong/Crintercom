package com.xmcrtech.intercom.avchat.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.xmcrtech.intercom.Constant;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;


public class IncallVideoFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    public static final String AUDIOTOVIDEO = "audiotovideo";//从视频切换过来的，需要检测原来的配置
    private static final String TAG = IncallVideoFragment.class.getSimpleName();
    private Object object;

    private boolean isClosedCamera = false;//标记当前摄像头的开关
    private View root;

    public static IncallVideoFragment newInstance() {
        return new IncallVideoFragment();
    }
    private String account = "";
    private boolean audiotovideo = false;

    private View switchAudio;//切换到视频
    private View switch_camera;//切换前置摄像头和后置摄像头
    private Chronometer time; //通话时间
    private ToggleButton muteTb;
    private ToggleButton speakerTb;
    private ToggleButton recordTb;
    private View hangup;

    private AVChatSurface avChatSurface;

    private AVChatActivity avchatActivity;

    private AVChatListener avChatUIListener;

    private boolean isInit = false;

    public void setAvChatUIListener(AVChatListener avChatUIListener) {
        this.avChatUIListener = avChatUIListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity){
            avchatActivity = (AVChatActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            account = bundle.getString(ACCOUNT);
            audiotovideo = bundle.getBoolean(AUDIOTOVIDEO);
            avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.incallvideo_frag, container, false);

        avChatSurface = new AVChatSurface(this.getContext(), root);

        isInit = true;
        Log.d(TAG,"onCreateView" + account);
        if(account != null){
            avChatSurface.initLargeSurfaceView(account);
        }
        switchAudio = root.findViewById(R.id.switch_audio);
        switchAudio.setOnClickListener(this);

        time = (Chronometer) root.findViewById(R.id.avchat_audio_time);

        switch_camera = root.findViewById(R.id.switch_camera);
        switch_camera.setOnClickListener(this);
        //通话控制布局
        muteTb = (ToggleButton) root.findViewById(R.id.muteTb);
        speakerTb = (ToggleButton) root.findViewById(R.id.speakerTb);
        recordTb = (ToggleButton) root.findViewById(R.id.recordTb);
        muteTb.setChecked(false);
        speakerTb.setChecked(false);
        recordTb.setChecked(false);

        hangup = root.findViewById(R.id.video_hangup);
        muteTb.setOnCheckedChangeListener(this);
        speakerTb.setOnCheckedChangeListener(this);
        recordTb.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

        //初始化本地图像
        avChatSurface.initSmallSurfaceView(Constant.getAccount());

        //如果有设置菜单，需要加这个
        setHasOptionsMenu(true);

        return root;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG,"音视頻切换了");
        if(!hidden){
            Log.d(TAG,"fragment is show");
            muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
            speakerTb.setChecked(AVChatManager.getInstance().speakerEnabled());
        }
    }

    /**
     * 监听通话发起后的状态
     */
    public AVChatStateObserver mAVChatStateObserver = new AVChatStateObserver() {
        @Override
        public void onTakeSnapshotResult(String s, boolean b, String s1) {

        }

        @Override
        public void onConnectionTypeChanged(int i) {

        }

        @Override
        public void onLocalRecordEnd(String[] strings, int i) {

        }

        @Override
        public void onFirstVideoFrameAvailable(String s) {

        }

        @Override
        public void onVideoFpsReported(String s, int i) {

        }

        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile) {

        }

        @Override
        public void onLeaveChannel() {

        }

        @Override
        public void onUserJoined(String s) {
            Log.d(TAG, "onUserJoin -> " + s + isInit);
            account = s;
            if(isInit){
                avChatSurface.initLargeSurfaceView(account);
            }
        }

        @Override
        public void onUserLeave(String s, int i) {

        }

        @Override
        public void onProtocolIncompatible(int i) {

        }

        @Override
        public void onDisconnectServer() {

        }

        @Override
        public void onNetworkQuality(String s, int i) {

        }

        @Override
        public void onCallEstablished() {
        }

        @Override
        public void onDeviceEvent(int i, String s) {

        }

        @Override
        public void onFirstVideoFrameRendered(String s) {

        }

        @Override
        public void onVideoFrameResolutionChanged(String s, int i, int i1, int i2) {

        }

        @Override
        public int onVideoFrameFilter(AVChatVideoFrame avChatVideoFrame) {
            return 0;
        }

        @Override
        public int onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
            return 0;
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if(audiotovideo){
            muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
            speakerTb.setChecked(AVChatManager.getInstance().speakerEnabled());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 挂断
     */
    private void hangUp() {
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "hangup onFailed->" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "hangup onException->" + exception);
            }
        });
        if(avchatActivity != null){
            avchatActivity.closeSessions(AVChatExitCode.HANGUP);
        }
        AVChatSoundPlayer.instance(getContext()).stop();
    }


    //关闭摄像头
    public void switchCamera(boolean open) {

        if(open){
            // 打开摄像头
            Log.d(TAG,"打开摄像头");
            AVChatManager.getInstance().muteLocalVideo(false);
            isClosedCamera = false;
            //avChatSurface.localVideoOn();
        }else {
            // 关闭摄像头
            Log.d(TAG,"关闭摄像头");
            AVChatManager.getInstance().muteLocalVideo(true);
            isClosedCamera = true;
            //avChatSurface.localVideoOff();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_hangup://挂断
                hangUp();
                break;
            case R.id.switch_audio:
                avChatUIListener.videoSwitchAudio();
            break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.close_camera:
                if(b){
                    switchCamera(true);
                }else{
                    switchCamera(false);
                }
            case R.id.muteTb:
                if (b) {
                    // 关闭音频
                    AVChatManager.getInstance().muteLocalAudio(true);
                } else {
                    // 打开音频
                    AVChatManager.getInstance().muteLocalAudio(false);
                }
                break;
            case R.id.speakerTb:
                if (b) {
                    //开启扬声器
                    AVChatManager.getInstance().setSpeaker(true);
                } else {
                    //关闭扬声器
                    AVChatManager.getInstance().setSpeaker(false);
                }
                break;
            case R.id.recordTb:
                if (b) {
                    Toast.makeText(getContext(), "功能未开放", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "功能未开放", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
