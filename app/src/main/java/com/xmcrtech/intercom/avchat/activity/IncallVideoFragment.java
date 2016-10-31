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

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.xmcrtech.intercom.Constant;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;


public class IncallVideoFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AVChatListener {

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    private static final String TAG = IncallVideoFragment.class.getSimpleName();
    private Object object;

    private boolean isClosedCamera = false;//标记当前摄像头的开关
    private View root;

    public static IncallVideoFragment newInstance() {
        return new IncallVideoFragment();
    }

    private String account = "";

    private View switchAudio;//切换到视频
    private View switch_camera;//切换前置摄像头和后置摄像头
    private Chronometer time; //通话时间
    private ToggleButton muteTb;
    private ToggleButton recordTb;
    private ToggleButton close_camera;
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
        if (context instanceof Activity) {
            avchatActivity = (AVChatActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            account = bundle.getString(ACCOUNT);
            avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }

        //注册监听
        registerNetCallObserver(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.incallvideo_frag, container, false);

        avChatSurface = new AVChatSurface(this.getContext(), root);



        isInit = true;
        Log.d(TAG, "onCreateView" + account);
        if (account != null) {
            avChatSurface.initLargeSurfaceView(account);
        }
        switchAudio = root.findViewById(R.id.switch_audio);
        switchAudio.setOnClickListener(this);

        close_camera = (ToggleButton)root.findViewById(R.id.close_camera);

        time = (Chronometer) root.findViewById(R.id.avchat_audio_time);

        switch_camera = root.findViewById(R.id.switch_camera);
        switch_camera.setOnClickListener(this);
        //通话控制布局
        muteTb = (ToggleButton) root.findViewById(R.id.muteTb);
        recordTb = (ToggleButton) root.findViewById(R.id.recordTb);
        muteTb.setChecked(false);
        recordTb.setChecked(false);

        hangup = root.findViewById(R.id.video_hangup);
        muteTb.setOnCheckedChangeListener(this);
        recordTb.setOnCheckedChangeListener(this);
        close_camera.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

        //初始化本地图像
        avChatSurface.initSmallSurfaceView(Constant.getAccount());

        //如果有设置菜单，需要加这个
        setHasOptionsMenu(true);

        return root;
    }


    /**
     * 注册监听
     *
     * @param register
     */
    private void registerNetCallObserver(boolean register) {

        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);//音视頻切换通知
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "音视頻切换了");
        if (!hidden) {
            Log.d(TAG, "fragment is show");
            muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
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
            if (isInit) {
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
        muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
        close_camera.setChecked(AVChatManager.getInstance().isLocalVideoMuted());

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerNetCallObserver(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_hangup://挂断
                onHangUp();
                break;
            case R.id.switch_audio:
                videoSwitchAudio();
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.close_camera:
                if (b) {
                    isClosedCamera = true;
                } else {
                    isClosedCamera = false;
                }
                closeCamera();
            case R.id.muteTb:
                if (b) {
                    // 关闭音频
                    AVChatManager.getInstance().muteLocalAudio(true);
                } else {
                    // 打开音频
                    AVChatManager.getInstance().muteLocalAudio(false);
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



    /**
     * 注册/注销网络通话控制消息（音视频模式切换通知）
     */
    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {

            switch (netCallControlNotification.getControlCommand()) {
                case SWITCH_AUDIO_TO_VIDEO:
                    Log.d(TAG,"对方请求切换到视频通话");

                    break;
                case SWITCH_VIDEO_TO_AUDIO:
                    Log.d(TAG,"对方请求切换到音频通话");
                    avChatUIListener.videoSwitchAudio();
                    break;
                case NOTIFY_VIDEO_OFF:
                    Log.d(TAG,"视频关闭");
                    avChatSurface.peerVideoOff();
                    break;
                case NOTIFY_VIDEO_ON:
                    Log.d(TAG,"视频开启");
                    avChatSurface.peerVideoOn();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onHangUp() {
        avChatUIListener.onHangUp();
    }

    @Override
    public void onRefuse() {

    }

    @Override
    public void onReceive() {

    }

    @Override
    public void toggleMute() {

    }

    @Override
    public void toggleSpeaker() {

    }

    @Override
    public void toggleRecord() {

    }

    @Override
    public void videoSwitchAudio() {
        avChatUIListener.videoSwitchAudio();
    }

    @Override
    public void audioSwitchVideo() {

    }

    @Override
    public void switchCamera() {
        AVChatManager.getInstance().switchCamera(); // 切换摄像头（主要用于前置和后置摄像头切换）
    }

    @Override
    public void closeCamera() {

        if(isClosedCamera){

            Log.d(TAG, "关闭摄像头");
            AVChatManager.getInstance().muteLocalVideo(true);
            avChatSurface.localVideoOff();

        }else {
            Log.d(TAG, "打开摄像头");
            AVChatManager.getInstance().muteLocalVideo(false);
            avChatSurface.localVideoOn();
        }
    }
}
