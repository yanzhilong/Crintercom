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
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;
import com.xmcrtech.intercom.avchat.AVChatListener;

public class IncallAudioFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    public static final String VIDEOTOAUDIO = "videotoaudio";//从视频切换过来的，需要检测原来的配置
    private static final String TAG = IncallAudioFragment.class.getSimpleName();

    public static IncallAudioFragment newInstance() {
        return new IncallAudioFragment();
    }

    private AVChatActivity avchatActivity;

    private String account = "";
    private boolean videotoaudio = false;

    private View switchVideo;//切换到视频
    private Chronometer time; //通话时间
    private ToggleButton muteTb;
    private ToggleButton speakerTb;
    private ToggleButton recordTb;
    private View hangup;

    private AVChatListener avChatUIListener;

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
            videotoaudio = bundle.getBoolean(VIDEOTOAUDIO);
            avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
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
        recordTb = (ToggleButton) root.findViewById(R.id.recordTb);
        muteTb.setChecked(true);
        speakerTb.setChecked(false);
        recordTb.setChecked(false);

        hangup = root.findViewById(R.id.audio_hangup);
        muteTb.setOnCheckedChangeListener(this);
        speakerTb.setOnCheckedChangeListener(this);
        recordTb.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

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

    @Override
    public void onResume() {
        super.onResume();
        if(videotoaudio){
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_hangup:
                hangUp();
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
}
