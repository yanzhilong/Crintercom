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

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;

public class IncallAudioFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

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
    private View hangup;
    private TextView status;//当前状态，正在通话中，正在等待对方同意
    private View switch_video_request;

    private AVChatListener avChatUIListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity){
            avchatActivity = (AVChatActivity) context;
            avChatUIListener = avchatActivity.getAvChatListener();
            avchatActivity.addCallStateChangeListener(new AVChatActivity.CallStateChangeListener() {
                @Override
                public void onCallStateChange(CallStateEnum callStateEnum) {
                    switch (callStateEnum){

                        case AUDIO:
                            status.setText("正在通话中");
                            switch_video_request.setVisibility(View.GONE);
                            break;
                        case INCOMING_AUDIO_TO_VIDEO://请求切换为视频
                            status.setText("对方请求切换到视频通话");
                            switch_video_request.setVisibility(View.VISIBLE);
                            break;
                        case OUTGOING_AUDIO_TO_VIDEO://正在邀请好友从語音切换到视频
                            status.setText("正在邀请好友视频通话");
                            break;
                        case OUTGOING_AUDIO_TO_VIDEO_FAIL://邀请请求失败
                            Toast.makeText(IncallAudioFragment.this.getContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
                            break;
                        case OUTGOING_AUDIO_TO_VIDEO_REJECT:
                            Toast.makeText(IncallAudioFragment.this.getContext(), R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
                            break;
                        case RECEIVE_AUDIO_TO_VIDEO_FAIL:
                            Toast.makeText(IncallAudioFragment.this.getContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            account = bundle.getString(ACCOUNT);
            //avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }
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
        speakerTb.setChecked(false);

        hangup = root.findViewById(R.id.audio_hangup);
        status = (TextView) root.findViewById(R.id.status);
        muteTb.setOnCheckedChangeListener(this);
        speakerTb.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

        switch_video_request = root.findViewById(R.id.switch_video_request);
        root.findViewById(R.id.receive).setOnClickListener(this);
        root.findViewById(R.id.refuse).setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_hangup:
                avChatUIListener.onHangUp();
                break;
            case R.id.switch_video:
                avChatUIListener.audioSwitchVideo();
                break;
            case R.id.receive:
                //同意切换到视频
                avChatUIListener.receiveSwitchAudioToVideo();
                break;
            case R.id.refuse:
                //拒绝切换到视频
                avChatUIListener.refuseaudioSwitchVideo();
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
                    avChatUIListener.micMute();
                } else {
                    avChatUIListener.micOpen();
                }
                break;
            case R.id.speakerTb:
                if (b) {
                    avChatUIListener.speakerOpen();
                } else {
                    avChatUIListener.speakerMute();
                }
                break;
        }
    }
}
