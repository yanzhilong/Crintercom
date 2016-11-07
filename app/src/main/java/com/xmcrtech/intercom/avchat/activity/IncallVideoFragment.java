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

import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.xmcrtech.intercom.Constant;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;
import com.xmcrtech.intercom.config.SessionConfig;


public class IncallVideoFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    public static final String AVCHATTYPE = "AvchatType";//当前通话类型

    private static final String TAG = IncallVideoFragment.class.getSimpleName();
    private Object object;

    private View root;

    public static IncallVideoFragment newInstance() {
        return new IncallVideoFragment();
    }

    private String account = "";
    //当前通话类型
    AVChatType avChatType;

    private Chronometer time; //通话时间
    private View hangup;
    private TextView nickname;
    private TextView callingmessage;
    private ToggleButton muteTb;


    private AVChatSurface avChatSurface;

    private AVChatActivity avchatActivity;

    private AVChatListener avChatUIListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            avchatActivity = (AVChatActivity) context;
            avchatActivity.addCallStateChangeListener(new AVChatActivity.CallStateChangeListener() {
                @Override
                public void onCallStateChange(CallStateEnum callStateEnum) {
                    switch (callStateEnum){
                        case VIDEO://通话中
                            changeCallStateEnum(CallStateEnum.VIDEO);
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
            avChatType = (AVChatType) bundle.getSerializable(AVCHATTYPE);
            avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.incallvideo_frag, container, false);

        nickname = (TextView) root.findViewById(R.id.nickname);
        callingmessage = (TextView) root.findViewById(R.id.callingmessage);
        avChatSurface = new AVChatSurface(this.getContext(), root);

        Log.d(TAG, "onCreateView" + account);

        time = (Chronometer) root.findViewById(R.id.avchat_audio_time);

        //通话控制布局
        muteTb = (ToggleButton) root.findViewById(R.id.muteTb);
        muteTb.setOnCheckedChangeListener(this);

        hangup = root.findViewById(R.id.video_hangup);
        hangup.setOnClickListener(this);

        //初始化本地图像

        //如果有设置菜单，需要加这个
        setHasOptionsMenu(true);

        return root;
    }

    public void changeCallStateEnum(CallStateEnum callStateEnum){

        switch (callStateEnum){

            case OUTGOING_VIDEO_CALLING:
                nickname.setText(account);
                callingmessage.setVisibility(View.VISIBLE);
                outGoingCalling(account,avChatType);
                break;
            case VIDEO:
                nickname.setVisibility(View.GONE);
                callingmessage.setVisibility(View.GONE);
                avChatSurface.initLargeSurfaceView(Constant.getAccount());
                break;
        }
    }


    /**
     * 拨打音视频
     */
    public void outGoingCalling(String account, AVChatType callTypeEnum) {

        AVChatSoundPlayer.instance(this.getContext()).play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);

        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = "extra_data";

        AVChatManager.getInstance().call(account, callTypeEnum, SessionConfig.newInstance(getContext()).getAvChatOptionalConfig(),
                notifyOption, new AVChatCallback<AVChatData>() {
                    @Override
                    public void onSuccess(AVChatData data) {
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.d(TAG, "avChat call failed code->" + code);

                        AVChatSoundPlayer.instance(getContext()).stop();

                        if (code == ResponseCode.RES_FORBIDDEN) {
                            Toast.makeText(getContext(), R.string.avchat_no_permission, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                        }
                        avChatUIListener.onHangUp();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Log.d(TAG, "avChat call onException->" + exception);
                        Toast.makeText(getContext(), R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                        avChatUIListener.onHangUp();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        changeCallStateEnum(CallStateEnum.OUTGOING_VIDEO_CALLING);
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
            case R.id.video_hangup://挂断
                avChatUIListener.onHangUp();
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
        }
    }
}
