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

public class IncallAudioFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ACCOUNT = "account";
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
    public void onResume() {
        super.onResume();
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
                    // 打开音频
                    AVChatManager.getInstance().muteLocalAudio(false);
                } else {
                    // 关闭音频
                    AVChatManager.getInstance().muteLocalAudio(true);
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
