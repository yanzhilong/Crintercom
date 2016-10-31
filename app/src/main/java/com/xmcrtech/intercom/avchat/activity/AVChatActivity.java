package com.xmcrtech.intercom.avchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;
import com.xmcrtech.intercom.avchat.AVChatUI;
import com.xmcrtech.intercom.util.NetworkUtil;

/**
 * 呼入和呼出的界面
 */
public class AVChatActivity extends AppCompatActivity implements AVChatUI.AVChatListener {

    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";//呼入数据　
    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";//是否是呼入

    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";//呼出类型，音頻、视频
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";//呼出账号
    private static final String TAG = AVChatActivity.class.getSimpleName();

    private AVChatData avChatData;//呼入数据

    private boolean mIsInComingCall = false;//是呼入还是呼出

    private int state; // 呼叫类型，音频或视频
    private boolean isCallEstablished = false; // 电话是否接通
    private String receiveraccount; // 呼出接收方的账号

    private IncomingFragment incomingFragment = IncomingFragment.newInstance();
    private IncallAudioFragment incallAudioFragment = IncallAudioFragment.newInstance();
    private IncallVideoFragment incallVideoFragment = IncallVideoFragment.newInstance();
    private OutGoingFragment outGoingFragment = OutGoingFragment.newInstance();
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View root = LayoutInflater.from(this).inflate(R.layout.avchat_act, null);
        setContentView(root);
        mIsInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
       /* Bundle bundle = new Bundle();
        bundle.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
        bundle.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
        incallAudioFragment.setArguments(bundle);

        Bundle bundle１ = new Bundle();
        bundle１.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
        bundle１.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
        incallVideoFragment.setArguments(bundle１);*/


        //判断来电还是去电
        if (mIsInComingCall) {
            avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
            state = avChatData.getChatType().getValue();
            receiveraccount = avChatData.getAccount();
            Log.d(TAG,"收到"+ receiveraccount + "的" + (state == AVChatType.AUDIO.getValue() ? "音频" : "视频")+"来电");
            inComingCalling();
        } else {
            receiveraccount = getIntent().getStringExtra(KEY_ACCOUNT);
            state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
            Log.d(TAG,"正在"+ (state == AVChatType.AUDIO.getValue() ? "音频" : "视频")+"呼叫"+receiveraccount);
            outgoingCalling();
        }
        isCallEstablished = false;//初始未接通
        //注册监听
        registerNetCallObserver(true);
    }

    /**
     * 音视频切换回調
     */
    private AVChatListener avChatListener = new AVChatListener() {
        @Override
        public void onHangUp() {

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
            closeSessions(AVChatExitCode.HANGUP);
            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
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
            AVChatManager.getInstance().requestSwitchToAudio(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // 界面布局切换。
               /* onCallStateChange(CallStateEnum.AUDIO);
                onVideoToAudio();*/
                    if(!incallAudioFragment.isAdded()){

                        Bundle bundle = new Bundle();
                        bundle.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
                        bundle.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                        incallAudioFragment.setArguments(bundle);

                    }
                    switchFragment(incallAudioFragment);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }

        @Override
        public void audioSwitchVideo() {

            /**
             * 请求音频切换到视频
             */
            AVChatManager.getInstance().requestSwitchToVideo(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "requestSwitchToVideo onSuccess");
                }

                @Override
                public void onFailed(int code) {
                    Log.d(TAG, "requestSwitchToVideo onFailed" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    Log.d(TAG, "requestSwitchToVideo onException" + exception);
                }
            });
        }

        @Override
        public void switchCamera() {

        }

        @Override
        public void closeCamera() {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();

    }

    //切换Fragment
    private void switchFragment(Fragment fragment){

        if (fragment != currentFragment) {
            if (!fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().hide(currentFragment)
                .add(R.id.framelayout, fragment,"audio")
                .commit();
            } else {
                getSupportFragmentManager().beginTransaction().hide(currentFragment)
                        .show(fragment).commit();
            }
            currentFragment = fragment;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerNetCallObserver(false);
    }

    /**
     * 呼出
     * @param context
     * @param account
     * @param callType
     */
    public static void outgoing(Context context, String account, int callType) {
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_IN_CALLING, false);
        intent.putExtra(KEY_CALL_TYPE, callType);
        context.startActivity(intent);
    }

    /**
     * 呼入调用
     * @param context
     * @param config
     */
    public static void incoming(Context context, AVChatData config) {
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_IN_CALLING, true);
        context.startActivity(intent);
    }

    /**
     * 接听
     */
    private void inComingCalling() {

        AVChatSoundPlayer.instance(getApplicationContext()).play(AVChatSoundPlayer.RingerTypeEnum.RING);

        Bundle bundle = new Bundle();
        bundle.putString(IncomingFragment.ACCOUNT,receiveraccount);
        bundle.putBoolean(IncomingFragment.ISVIDEO,state == AVChatType.VIDEO.getValue());
        incomingFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.framelayout, incomingFragment).commit();
        //currentFragment = incomingFragment;
    }


    /**
     * 拨打
     */
    private void outgoingCalling() {
        if (!NetworkUtil.isNetAvailable(AVChatActivity.this)) { // 网络不可用
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(OutGoingFragment.ACCOUNT,receiveraccount);
        bundle.putSerializable(OutGoingFragment.AVCHATTYPE,AVChatType.typeOfValue(state));
        bundle.putSerializable(OutGoingFragment.AVCHATLISTENER,avChatListener);
        outGoingFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.framelayout, outGoingFragment).commit();

    }


    /**
     * 注册监听
     *
     * @param register
     */
    private void registerNetCallObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(mAVChatStateObserver, register);//监听通话状态
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);//拔出时,监听被叫方的回应　
        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);//音视頻切换通知
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);//被叫的时候对方挂断，通话的时候对方挂断通知
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);//接听的时候被其它客户端接听了
        AVChatManager.getInstance().observeTimeoutNotification(timeoutObserver, register);//网絡出錯，或对方呼入的时候自己未接听
        AVChatManager.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register);
    }


    Observer<Integer> autoHangUpForLocalPhoneObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {

            closeSessions(AVChatExitCode.PEER_BUSY);
            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
            Log.d(TAG,"本地自动挂断");
        }
    };


    Observer<AVChatTimeOutEvent> timeoutObserver = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent event) {
            if (event == AVChatTimeOutEvent.NET_BROKEN_TIMEOUT) {
                //网絡错误
                closeSessions(AVChatExitCode.NET_ERROR);
                Log.d(TAG,"在呼叫过程中网絡出錯");
            } else {
                Log.d(TAG,"对方未接听或自己未接听");
                closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
            }

            // 来电超时，自己未接听
            if (event == AVChatTimeOutEvent.INCOMING_TIMEOUT) {
                Log.d(TAG,"呼入的时候自己未接听，显示在通知栏");
                closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
            }

            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
        }
    };


    /**
     * 注册/注销同时在线的其他端对主叫方的响应
     */
    Observer<AVChatOnlineAckEvent> onlineAckObserver = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent ackInfo) {

            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
            Log.d(TAG,"呼入的来电被其它客户端接听了");
            if (ackInfo.getClientType() != ClientType.Android) {
                String client = null;
                switch (ackInfo.getClientType()) {
                    case ClientType.Web:
                        client = "Web";
                        break;
                    case ClientType.Windows:
                        client = "Windows";
                        break;
                    default:
                        break;
                }
                if (client != null) {
                    String option = ackInfo.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
                    Toast.makeText(AVChatActivity.this, "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
                    closeSessions(-1);
                }
            }
        }
    };

    /**
     * 注册/注销网络通话对方挂断的通知
     */
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {

            if(isCallEstablished){
                closeSessions(AVChatExitCode.HANGUP);
            }else{
                closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
            }
            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
            Log.d(TAG,isCallEstablished ? "对话中" : "呼入中" + "对方挂断了");

        }
    };

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
                case SWITCH_AUDIO_TO_VIDEO_AGREE:
                    Log.d(TAG,"对方同意切换到视频通话");
                    if(!incallVideoFragment.isAdded()){
                        Bundle bundle１ = new Bundle();
                        bundle１.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
                        bundle１.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                        incallVideoFragment.setArguments(bundle１);
                    }
                    switchFragment(incallVideoFragment);
                    break;
                case SWITCH_AUDIO_TO_VIDEO_REJECT:
                    Log.d(TAG,"请求切换到视频被拒绝");
                    break;
                case SWITCH_VIDEO_TO_AUDIO:
                    Log.d(TAG,"对方请求切换到音频通话");
                    break;
                case NOTIFY_VIDEO_OFF:
                    Log.d(TAG,"视频关闭");
                    break;
                case NOTIFY_VIDEO_ON:
                    Log.d(TAG,"视频开启");
                    break;
                case NOTIFY_RECORD_START:
                    Toast.makeText(AVChatActivity.this, "对方开始了通话录制", Toast.LENGTH_SHORT).show();
                    break;
                case NOTIFY_RECORD_STOP:
                    Toast.makeText(AVChatActivity.this, "对方结束了通话录制", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 注册/注销网络通话被叫方的响应（接听、拒绝、忙）
     */
    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {

            Log.d(TAG,"停止呼出铃声");
            AVChatSoundPlayer.instance(AVChatActivity.this).stop();

            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {

                AVChatSoundPlayer.instance(AVChatActivity.this).play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
                closeSessions(AVChatExitCode.PEER_BUSY);
                //对方线路忙
                Log.d(TAG,"对方线路忙");
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                closeSessions(AVChatExitCode.REJECT);
                Log.d(TAG,"对方线拒绝接听");

            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                if (ackInfo.isDeviceReady()) {

                    Log.d(TAG,"对方同意接听");
                } else {
                    // 设备初始化失败
                    Toast.makeText(AVChatActivity.this, R.string.avchat_device_no_ready, Toast.LENGTH_SHORT).show();
                    closeSessions(AVChatExitCode.OPEN_DEVICE_ERROR);
                }
            }
        }
    };


    /**
     * 监听通话发起后的状态
     */
    private AVChatStateObserver mAVChatStateObserver = new AVChatStateObserver() {
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

            Log.d(TAG, "服务器连接码" + code);
            if (code == 200) {
                Log.d(TAG, "连接服务器成功");
            } else if (code == 101) { // 连接超时
                closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
                Log.d(TAG, "连接服务器超时");
            } else if (code == 401) { // 验证失败
                closeSessions(AVChatExitCode.CONFIG_ERROR);
                Log.d(TAG, "验证失败");
            } else if (code == 417) { // 无效的channelId
                closeSessions(AVChatExitCode.INVALIDE_CHANNELID);
                Log.d(TAG, "无效的channelId");
            } else { // 连接服务器错误，直接退出
                closeSessions(AVChatExitCode.CONFIG_ERROR);
                Log.d(TAG, "连接服务器错误");
            }

        }

        @Override
        public void onLeaveChannel() {

        }

        @Override
        public void onUserJoined(String s) {
            Log.d(TAG, "onUserJoin -> " + s);
            incallVideoFragment.mAVChatStateObserver.onUserJoined(s);
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
            Log.d(TAG,state == AVChatType.AUDIO.getValue() ? "音频" : "视频" + "通话已经接通");
            //切换到相应界面
            if (state == AVChatType.AUDIO.getValue()) {
                Bundle bundle = new Bundle();
                bundle.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
                bundle.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                incallAudioFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, incallAudioFragment).commit();
                currentFragment = incallAudioFragment;
            } else {
                Bundle bundle１ = new Bundle();
                bundle１.putString(IncallAudioFragment.ACCOUNT,receiveraccount);
                bundle１.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                incallVideoFragment.setArguments(bundle１);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, incallVideoFragment).commit();
                currentFragment = incallVideoFragment;
            }
            isCallEstablished = true;
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
    public void uiExit() {
        finish();
    }

    /**
     * 着装会話
     */
    public void closeSessions(int exitCode){
        showQuitToast(exitCode);
        finish();
    }

    /**
     * 给出结束的提醒
     *
     * @param code
     */
    public void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(this, R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_HANGUP:
            case AVChatExitCode.HANGUP:
                Toast.makeText(this, R.string.avchat_call_finish, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_BUSY:
                Toast.makeText(this, R.string.avchat_peer_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_PEER_LOWER:
                Toast.makeText(this, R.string.avchat_peer_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_SELF_LOWER:
                Toast.makeText(this, R.string.avchat_local_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.INVALIDE_CHANNELID:
                Toast.makeText(this, R.string.avchat_invalid_channel_id, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.LOCAL_CALL_BUSY:
                Toast.makeText(this, R.string.avchat_local_call_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_NO_RESPONSE:
                //来电未接看是否显示通知

                break;
            default:
                break;
        }
    }
}
