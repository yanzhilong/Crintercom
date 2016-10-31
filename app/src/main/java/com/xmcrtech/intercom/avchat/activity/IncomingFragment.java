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
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;
import com.xmcrtech.intercom.config.SessionConfig;

/**
 * 有电话呼入的时的界面
 */
public class IncomingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = IncomingFragment.class.getSimpleName();

    public static final String ACCOUNT = "account";
    public static final String ISVIDEO = "isvideo";

    private TextView nickname;//备注名称
    private TextView request;//请求类型

    private String account = "";
    private boolean isVideo = false;
    private CallStateEnum callingState = CallStateEnum.INVALID;

    private AVChatActivity avchatActivity;

    public static IncomingFragment newInstance() {
        return new IncomingFragment();
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
            isVideo = bundle.getBoolean(ISVIDEO);
            callingState = isVideo ? CallStateEnum.INCOMING_VIDEO_CALLING : CallStateEnum.INCOMING_AUDIO_CALLING;
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.incoming_frag, container, false);

        nickname = (TextView) root.findViewById(R.id.nickname);
        request = (TextView) root.findViewById(R.id.request);
        root.findViewById(R.id.refuse).setOnClickListener(this);
        root.findViewById(R.id.receive).setOnClickListener(this);

        if (isVideo) {
            request.setText("请求视频聊天");
        } else {
            request.setText("请求通话");
        }

        nickname.setText(account);
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

    //语音接听
    private void onAudioReceive(){

        AVChatManager.getInstance().accept(SessionConfig.newInstance(getContext()).getAvChatOptionalConfig(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void v) {
                Log.d(TAG, "接听来电成功");
            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(getContext(), "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "接听音頻电话失败");
                if(avchatActivity != null){
                    avchatActivity.closeSessions(AVChatExitCode.CANCEL);
                }
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "接听音頻电话异常:" + exception);
            }
        });
    }

    //视频接听
    private void onVideoReceive(){
        onAudioReceive();
    }

    /**
     * 拒绝来电
     */
    private void rejectInComingCall() {
        /**
         * 接收方拒绝通话
         * AVChatCallback 回调函数
         */
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "reject sucess->" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "reject sucess");
            }
        });
        if(avchatActivity != null){
            avchatActivity.closeSessions(AVChatExitCode.REJECT);
        }
        //closeSessions(AVChatExitCode.REJECT);
        AVChatSoundPlayer.instance(getContext()).stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refuse://拒绝
                rejectInComingCall();
                break;
            case R.id.receive://接听
                switch (callingState){
                    case INCOMING_AUDIO_CALLING:
                        onAudioReceive();
                        callingState = CallStateEnum.AUDIO_CONNECTING;
                        break;
                    case INCOMING_VIDEO_CALLING:
                        onVideoReceive();
                        callingState = CallStateEnum.VIDEO_CONNECTING;
                        break;
                }
                onConnecting();//连接中
                AVChatSoundPlayer.instance(getContext()).stop();
                break;
            default:
                break;
        }
    }

    //连接中ui
    private void onConnecting() {
        request.setText("连接中");
    }
}
