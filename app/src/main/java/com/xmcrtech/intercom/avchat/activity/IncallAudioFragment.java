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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;

public class IncallAudioFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AVChatListener{

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    private static final String TAG = IncallAudioFragment.class.getSimpleName();

    public static IncallAudioFragment newInstance() {
        return new IncallAudioFragment();
    }

    private AVChatActivity avchatActivity;

    private String account = "";

    private View switchVideo;//切换到视频
    private Chronometer time; //通话时间
    private ToggleButton muteTb;
    private ToggleButton speakerTb;
    private ToggleButton recordTb;
    private View hangup;
    private TextView status;//当前状态，正在通话中，正在等待对方同意
    private View switch_video_request;

    private AVChatListener avChatUIListener;

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
            avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }
        //注册监听
        registerNetCallObserver(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.incallaudio_frag, container, false);

        switchVideo = root.findViewById(R.id.switch_video);
        switchVideo.setOnClickListener(this);

        time = (Chronometer) root.findViewById(R.id.avchat_audio_time);

        //通话控制布局
        muteTb = (ToggleButton) root.findViewById(R.id.muteTb);
        speakerTb = (ToggleButton) root.findViewById(R.id.speakerTb);
        recordTb = (ToggleButton) root.findViewById(R.id.recordTb);
        speakerTb.setChecked(false);
        recordTb.setChecked(false);

        hangup = root.findViewById(R.id.audio_hangup);
        status = (TextView) root.findViewById(R.id.status);
        muteTb.setOnCheckedChangeListener(this);
        speakerTb.setOnCheckedChangeListener(this);
        recordTb.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

        switch_video_request = root.findViewById(R.id.switch_video_request);
        root.findViewById(R.id.receive).setOnClickListener(this);
        root.findViewById(R.id.refuse).setOnClickListener(this);

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
        Log.d(TAG,"音视頻切换了");
        if(!hidden){
            Log.d(TAG,"fragment is show");
            status.setText("正在通话中");
            muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
            speakerTb.setChecked(AVChatManager.getInstance().speakerEnabled());
        }else{
            switch_video_request.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        muteTb.setChecked(AVChatManager.getInstance().isLocalAudioMuted());
        speakerTb.setChecked(AVChatManager.getInstance().speakerEnabled());
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

    /**
     * 注册/注销网络通话控制消息（音视频模式切换通知）
     */
    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {

            switch (netCallControlNotification.getControlCommand()) {
                case SWITCH_AUDIO_TO_VIDEO:
                    Log.d(TAG,"对方请求切换到视频通话");
                    status.setText("对方请求切换到视频通话");
                    switch_video_request.setVisibility(View.VISIBLE);
                    break;
                case SWITCH_AUDIO_TO_VIDEO_REJECT:
                    Log.d(TAG,"请求切换到视频被拒绝");
                    status.setText("正在通话中");
                    Toast.makeText(IncallAudioFragment.this.getContext(), R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_hangup:
                onHangUp();
                break;

            case R.id.switch_video:
                audioSwitchVideo();
                break;
            case R.id.receive:
                //同意切换到视频
                status.setText("正在通话中");
                receiveSwitchAudioToVideo();
                break;
            case R.id.refuse:
                //拒绝切换到视频
                status.setText("正在通话中");
                AVChatManager.getInstance().ackSwitchToVideo(false, null); // 音频切换到视频请求的回应. true为同意，false为拒绝
                switch_video_request.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
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

    }

    @Override
    public void audioSwitchVideo() {
        status.setText("正在等待对方同意");
        avChatUIListener.audioSwitchVideo();
    }

    @Override
    public void switchCamera() {

    }

    @Override
    public void closeCamera() {

    }

    @Override
    public void receiveSwitchAudioToVideo() {
        avChatUIListener.receiveSwitchAudioToVideo();
    }
}
