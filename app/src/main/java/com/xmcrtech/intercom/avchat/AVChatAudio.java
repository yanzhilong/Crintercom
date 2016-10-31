package com.xmcrtech.intercom.avchat;

import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;

/**
 * 音频通话相关管理类
 * Created by yanzl on 16-10-25.
 */
public class AVChatAudio implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    //网絡速度
    private static final int[] NETWORK_GRADE_LABEL = new int[]{R.string.avchat_network_grade_0,R.string.avchat_network_grade_1,R.string.avchat_network_grade_2,R.string.avchat_network_grade_3};
    private static final String TAG = AVChatAudio.class.getSimpleName();

    // view
    private View rootView ;
    private View switchVideo;//切换到视频
    private TextView nickNameTV; //昵称
    private Chronometer time; //通话时间
    private TextView wifiUnavailableNotifyTV; //非wifi环境友情提醒
    private TextView notifyTV; //对方请求视频聊天
    private TextView netUnstableTV;//当前的网絡状态

    private View mute_speaker_hangup; //通话控制，静音，录制、挂断....
    private ToggleButton muteTb;
    private ToggleButton speakerTb;
    private ToggleButton recordTb;
    private View hangup;

    //record
    private View recordView;  //录制布局
    private View recordTip;   //录制中textview
    private View recordWarning; //内存不足警告

    private View refuse_receive;
    private TextView refuseTV;
    private TextView receiveTV;

    private AVChatListener listener;//当前状态监听
    private AVChatUI manager;//音视頻管理类，用于获取时间及相差参数

    // state
    private boolean init = false;//用于判断是否初始化索

    public AVChatAudio(View root, AVChatListener listener, AVChatUI manager) {
        this.rootView = root;
        this.listener = listener;
        this.manager = manager;
    }

    /**
     * 界面初始化
     */
    private void findViews() {
        if(init || rootView == null){
            return;
        }
        switchVideo = rootView.findViewById(R.id.avchat_audio_switch_video);
        switchVideo.setOnClickListener(this);

        nickNameTV = (TextView) rootView.findViewById(R.id.avchat_audio_nickname);
        time = (Chronometer) rootView.findViewById(R.id.avchat_audio_time);
        wifiUnavailableNotifyTV = (TextView) rootView.findViewById(R.id.avchat_audio_wifi_unavailable);
        notifyTV = (TextView) rootView.findViewById(R.id.avchat_audio_notify);
        netUnstableTV = (TextView) rootView.findViewById(R.id.avchat_audio_netunstable);

        //通话控制布局
        mute_speaker_hangup = rootView.findViewById(R.id.avchat_audio_mute_speaker_huangup);
        muteTb = (ToggleButton) mute_speaker_hangup.findViewById(R.id.muteTb);
        speakerTb = (ToggleButton) mute_speaker_hangup.findViewById(R.id.speakerTb);
        recordTb = (ToggleButton) mute_speaker_hangup.findViewById(R.id.recordTb);
        hangup = mute_speaker_hangup.findViewById(R.id.avchat_audio_hangup);
        muteTb.setOnCheckedChangeListener(this);
        speakerTb.setOnCheckedChangeListener(this);
        recordTb.setOnCheckedChangeListener(this);
        hangup.setOnClickListener(this);

        refuse_receive = rootView.findViewById(R.id.avchat_audio_refuse_receive);
        refuseTV = (TextView) refuse_receive.findViewById(R.id.refuse);
        receiveTV = (TextView) refuse_receive.findViewById(R.id.receive);
        refuseTV.setOnClickListener(this);
        receiveTV.setOnClickListener(this);

        recordView = rootView.findViewById(R.id.avchat_record_layout);
        recordTip = rootView.findViewById(R.id.avchat_record_tip);
        recordWarning = rootView.findViewById(R.id.avchat_record_warning);

        init = true;
    }


    /**
     * 显示或隐藏音视频切换
     * @param visible
     */
    private void setSwitchVideo(boolean visible){
        switchVideo.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 个人信息设置
     */
    private void showProfile(){
        String account = manager.getAccount();
        nickNameTV.setText(account);
    }

    /**
     * 界面状态文案设置
     * @param resId 文案
     */
    private void showNotify(int resId) {
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    /**
     * 显示或隐藏禁音，结束通话布局
     * @param visible
     */
    private void setMuteSpeakerHangupControl(boolean visible){
        mute_speaker_hangup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示或隐藏拒绝，开启布局
     * @param visible
     */
    private void setRefuseReceive(boolean visible){
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 音视频状态变化及界面刷新
     * @param state
     */
    public void onCallStateChange(CallStateEnum state){
        if(CallStateEnum.isAudioMode(state))
            findViews();
        switch (state){
            case OUTGOING_AUDIO_CALLING: //拨打出的免费通话
                /*setSwitchVideo(false);
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_wait_recieve);
                setWifiUnavailableNotifyTV(true);
                setMuteSpeakerHangupControl(true);
                setRefuseReceive(false);*/
                break;
            case INCOMING_AUDIO_CALLING://呼入免费通话请求
                setSwitchVideo(false);
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_audio_call_request);
                setMuteSpeakerHangupControl(false);
                setRefuseReceive(true);
                receiveTV.setText(R.string.avchat_pickup);
                break;
            case AUDIO://正在进行语音通话
                Log.d(TAG,"正在进行语音通话");
                /*setWifiUnavailableNotifyTV(false);
                showNetworkCondition(1);
                showProfile();
                setSwitchVideo(true);
                setTime(true);
                hideNotify();
                setMuteSpeakerHangupControl(true);
                setRefuseReceive(false);
                enableToggle();*/
                break;
            case AUDIO_CONNECTING://语音通话连接中
                /*showNotify(R.string.avchat_connecting);*/
                break;
            case INCOMING_AUDIO_TO_VIDEO://对方邀请切换成视频
                /*showNotify(R.string.avchat_audio_to_video_invitation);
                setMuteSpeakerHangupControl(false);
                setRefuseReceive(true);*/
                receiveTV.setText(R.string.avchat_receive);
                break;
            default:
                break;
        }
        setRoot(CallStateEnum.isAudioMode(state));
    }

    /**
     * ***************************** 布局显隐设置 ***********************************
     */

    private void setRoot(boolean visible){
        rootView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.muteTb:
                if(b){

                }else {

                }
                break;
            case R.id.speakerTb:
                if(b){

                }else {

                }
                break;
            case R.id.recordTb:
                if(b){

                }else {

                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.avchat_audio_hangup:
                //挂断

                break;
            case R.id.receive:
                //接听

                listener.onReceive();
                break;
        }
    }
}
