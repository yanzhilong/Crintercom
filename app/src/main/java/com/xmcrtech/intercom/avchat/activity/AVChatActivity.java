package com.xmcrtech.intercom.avchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;
import com.xmcrtech.intercom.config.SessionConfig;
import com.xmcrtech.intercom.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 呼入和呼出的界面
 */
public class AVChatActivity extends AppCompatActivity{

    private static final String TAG = AVChatActivity.class.getSimpleName();

    private static final String AVCHATDATA = "AVChatData";//呼入数据
    private static final String CALLSTATE = "CallState";//当前状态
    private static final String AVCHATTYPE = "AvchatType";//当前通话类型
    private static final String REQUEST_ACCOUNT = "request_account";//呼出账号

    private String requestaccount; // 呼出或呼入方的账号
    private boolean callsuccess = false;//是否通话成功标志位

    private IncomingFragment incomingFragment = IncomingFragment.newInstance();
    private IncallAudioFragment incallAudioFragment = IncallAudioFragment.newInstance();
    private IncallVideoFragment incallVideoFragment = IncallVideoFragment.newInstance();
    private OutGoingFragment outGoingFragment = OutGoingFragment.newInstance();
    private Fragment currentFragment;

    //当前通话状态数据
    CallStateEnum callStateEnum = CallStateEnum.INVALID;//默认未知状态
    //当前通话类型
    AVChatType avChatType;

    CallStateChangeListener callStateChangeListener;//当前状态变化监听

    List<CallStateChangeListener> callStateChangeListeners = new ArrayList<>();

    //增加监听
    public void addCallStateChangeListener(CallStateChangeListener callStateChangeListener){
        callStateChangeListeners.add(callStateChangeListener);
    }

    public void removeCallStateChangeListener(CallStateChangeListener callStateChangeListener){
        callStateChangeListeners.remove(callStateChangeListener);
    }

    public interface CallStateChangeListener{

        void onCallStateChange(CallStateEnum callStateEnum);
    }

    public void changeCallStateEnum(CallStateEnum callStateEnum){

        this.callStateEnum = callStateEnum;
        for(CallStateChangeListener callStateChangeListener : callStateChangeListeners){
            callStateChangeListener.onCallStateChange(callStateEnum);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View root = LayoutInflater.from(this).inflate(R.layout.avchat_act, null);
        setContentView(root);

        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);

        callStateEnum = (CallStateEnum) getIntent().getSerializableExtra(CALLSTATE);

        //判断来电还是去电
        switch (callStateEnum){
            case INCOMING://来电

                AVChatData avChatData = (AVChatData) getIntent().getSerializableExtra(AVCHATDATA);
                requestaccount = avChatData.getAccount();
                avChatType = avChatData.getChatType();
                inComingCalling();
                callStateEnum = avChatType == AVChatType.AUDIO ? CallStateEnum.AUDIOINCOMING : CallStateEnum.VIDEOINCOMING;
                Log.d(TAG,"收到"+ requestaccount + "的" + (avChatType == AVChatType.AUDIO ? "音频" : "视频")+"来电");

                break;
            case OUTGOING://去电

                requestaccount = getIntent().getStringExtra(REQUEST_ACCOUNT);
                avChatType = (AVChatType) getIntent().getSerializableExtra(AVCHATTYPE);
                outgoingCalling();
                callStateEnum = avChatType == AVChatType.AUDIO ? CallStateEnum.AUDIOOUTGOING : CallStateEnum.VIDEOOUTGOING;
                Log.d(TAG,"正在"+ (avChatType == AVChatType.AUDIO ? "音频" : "视频")+"呼叫"+ requestaccount);
                break;
        }

        //注册监听
        registerNetCallObserver(true);
    }

    public AVChatListener getAvChatListener() {
        return avChatListener;
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

        // 关闭音频
        @Override
        public void toggleMute() {

        }

        // 打开音频
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
                    startAudioSession();
                }

                @Override
                public void onFailed(int code) {
                    changeCallStateEnum(CallStateEnum.OUTGOING_VIDEO_TO_AUDIO_FAIL);
                    changeCallStateEnum(CallStateEnum.VIDEO);
                }

                @Override
                public void onException(Throwable exception) {
                    changeCallStateEnum(CallStateEnum.OUTGOING_VIDEO_TO_AUDIO_FAIL);
                    changeCallStateEnum(CallStateEnum.VIDEO);
                }
            });
        }

        @Override
        public void audioSwitchVideo() {

            /**
             * 请求音频切换到视频
             */
            changeCallStateEnum(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO);
            AVChatManager.getInstance().requestSwitchToVideo(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "requestSwitchToVideo onSuccess");
                }

                @Override
                public void onFailed(int code) {
                    Log.d(TAG, "requestSwitchToVideo onFailed" + code);
                    changeCallStateEnum(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO_FAIL);
                    changeCallStateEnum(CallStateEnum.AUDIO);
                }

                @Override
                public void onException(Throwable exception) {
                    Log.d(TAG, "requestSwitchToVideo onException" + exception);
                    changeCallStateEnum(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO_FAIL);
                    changeCallStateEnum(CallStateEnum.AUDIO);
                }
            });

        }

        @Override
        public void switchCamera() {

        }

        @Override
        public void closeCamera() {

        }

        @Override
        public void refuseaudioSwitchVideo() {
            AVChatManager.getInstance().ackSwitchToVideo(false, null); // 音频切换到视频请求的回应. true为同意，false为拒绝
            changeCallStateEnum(CallStateEnum.AUDIO);
        }

        @Override
        public void micMute() {
            AVChatManager.getInstance().muteLocalAudio(true);
        }

        @Override
        public void micOpen() {
            AVChatManager.getInstance().muteLocalAudio(false);
        }

        //关闭扬声器
        @Override
        public void speakerMute() {
            AVChatManager.getInstance().setSpeaker(false);
        }

        //开启扬声器
        @Override
        public void speakerOpen() {
            AVChatManager.getInstance().setSpeaker(true);
        }

        //拒绝接听
        @Override
        public void refuse() {
            onAVChatRefuse();
        }

        //接听来电
        @Override
        public void answer() {
            switch (callStateEnum){
                case AUDIOINCOMING:
                case VIDEOINCOMING:
                    onAVChatAnswer();
                    break;
            }
        }


        @Override
        public void receiveSwitchAudioToVideo() {
            AVChatManager.getInstance().ackSwitchToVideo(true, new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startVideoSession();
                }

                @Override
                public void onFailed(int code) {
                    changeCallStateEnum(CallStateEnum.RECEIVE_AUDIO_TO_VIDEO_FAIL);
                    changeCallStateEnum(CallStateEnum.AUDIO);
                }

                @Override
                public void onException(Throwable exception) {
                    changeCallStateEnum(CallStateEnum.RECEIVE_AUDIO_TO_VIDEO_FAIL);
                    changeCallStateEnum(CallStateEnum.AUDIO);
                }
            });

        }
    };

    //启动视频会话
    private void startVideoSession(){
        if(!incallVideoFragment.isAdded()){
            Bundle bundle１ = new Bundle();
            bundle１.putString(IncallAudioFragment.ACCOUNT, requestaccount);
            //bundle１.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
            incallVideoFragment.setArguments(bundle１);
        }
        switchFragment(incallVideoFragment);
        changeCallStateEnum(CallStateEnum.VIDEO);
    }

    //启动音频会话
    private void startAudioSession(){
        if(!incallAudioFragment.isAdded()){

            Bundle bundle = new Bundle();
            bundle.putString(IncallAudioFragment.ACCOUNT, requestaccount);
            //bundle.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
            incallAudioFragment.setArguments(bundle);

        }
        switchFragment(incallAudioFragment);
        changeCallStateEnum(CallStateEnum.AUDIO);
    }


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
     * @param avChatType
     */
    public static void outgoing(Context context, String account, AVChatType avChatType) {
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.putExtra(REQUEST_ACCOUNT, account);
        intent.putExtra(CALLSTATE, CallStateEnum.OUTGOING);
        intent.putExtra(AVCHATTYPE, avChatType);
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
        intent.putExtra(AVCHATDATA, config);
        intent.putExtra(CALLSTATE, CallStateEnum.INCOMING);
        context.startActivity(intent);
    }

    /**
     * 接听
     */
    private void inComingCalling() {

        AVChatSoundPlayer.instance(getApplicationContext()).play(AVChatSoundPlayer.RingerTypeEnum.RING);

        Bundle bundle = new Bundle();
        bundle.putString(IncomingFragment.ACCOUNT, requestaccount);
        //bundle.putSerializable(IncomingFragment.AVCHATLISTENER,avChatListener);
        bundle.putSerializable(IncomingFragment.AVCHATTYPE,avChatType);
        incomingFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.framelayout, incomingFragment).commit();
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
        bundle.putString(OutGoingFragment.ACCOUNT, requestaccount);
        bundle.putSerializable(OutGoingFragment.AVCHATTYPE,avChatType);
        //bundle.putSerializable(OutGoingFragment.AVCHATLISTENER,avChatListener);
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

            AVChatSoundPlayer.instance(AVChatActivity.this).stop();
            if(callsuccess){
                closeSession(AvchatExitEnum.HANGUP);
            }else{
                closeSession(AvchatExitEnum.NORESPONSE);
            }
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
                    changeCallStateEnum(CallStateEnum.INCOMING_AUDIO_TO_VIDEO);
                    break;
                case SWITCH_AUDIO_TO_VIDEO_AGREE:
                    Log.d(TAG,"对方同意切换到视频通话");
                    startVideoSession();
                    break;
                case SWITCH_AUDIO_TO_VIDEO_REJECT:
                    Log.d(TAG,"请求切换到视频被拒绝");
                    changeCallStateEnum(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO_REJECT);
                    changeCallStateEnum(CallStateEnum.AUDIO);
                    break;
                case SWITCH_VIDEO_TO_AUDIO:
                    Log.d(TAG,"对方请求切换到音频通话");
                    avChatListener.videoSwitchAudio();
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

            callsuccess = true;//标记通话成功
            Log.d(TAG,avChatType == AVChatType.AUDIO ? "音频" : "视频" + "通话已经接通");
            //切换到相应界面
            if (avChatType == AVChatType.AUDIO) {

                changeCallStateEnum(CallStateEnum.AUDIO);
                Bundle bundle = new Bundle();
                bundle.putString(IncallAudioFragment.ACCOUNT, requestaccount);
                //bundle.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                incallAudioFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, incallAudioFragment).commit();
                currentFragment = incallAudioFragment;
            } else {

                changeCallStateEnum(CallStateEnum.VIDEO);
                Bundle bundle１ = new Bundle();
                bundle１.putString(IncallAudioFragment.ACCOUNT, requestaccount);
                //bundle１.putSerializable(IncallVideoFragment.AVCHATLISTENER,avChatListener);
                incallVideoFragment.setArguments(bundle１);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, incallVideoFragment).commit();
                currentFragment = incallVideoFragment;
            }
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

    /**
     * 着装会話
     */
    public void closeSessions(int exitCode){
        showQuitToast(exitCode);
        finish();
    }

    /**
     * 着装会話
     */
    public void closeSession(AvchatExitEnum avchatExitEnum){

        if(this.isFinishing()){
            return;
        }
        switch (avchatExitEnum){
            case OTHER:

                break;
            default:
                Toast.makeText(this, avchatExitEnum.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
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

    /**
     * 语音接听亚电
     */
    public void onAVChatAnswer(){

        AVChatSoundPlayer.instance(this).stop();
        changeCallStateEnum(CallStateEnum.ANSWERCONNECTING);
        AVChatManager.getInstance().accept(SessionConfig.newInstance(this).getAvChatOptionalConfig(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void v) {
                Log.d(TAG, "接听来电成功");
            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    closeSession(AvchatExitEnum.LOCALDEVICEFAIL);
                } else {
                    closeSession(AvchatExitEnum.CONNECTIONFAIL);
                }
                Log.d(TAG, "接听来电失败");
            }

            @Override
            public void onException(Throwable exception) {
                closeSession(AvchatExitEnum.UNKNOWEXCEPTION);
                Log.d(TAG, "接听来电异常:" + exception);
            }
        });
    }

    /**
     * 拒绝来电
     */
    public void onAVChatRefuse(){

        AVChatSoundPlayer.instance(this).stop();
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
        closeSession(AvchatExitEnum.OTHER);
    }

}
