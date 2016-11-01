package com.xmcrtech.intercom.avchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.activity.AVChatExitCode;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音视频管理器, 音视频相关功能管理
 * Created by yanzl on 16-10-24.
 */
public class AVChatUI implements AVChatListener {

    private static final String TAG = AVChatUI.class.getSimpleName();
    private Context context;
    private AVChatData avChatData;
    private final AVChatListener aVChatListener;

    private long timeBase = 0;
    private AVChatAudio avChatAudio;

    private CallStateEnum callingState = CallStateEnum.INVALID;

    private String receiveraccount; // 呼出接收方的账号
    public AtomicBoolean isCallEstablish = new AtomicBoolean(false);//用于判断是否有建立连接
    // state
    public boolean canSwitchCamera = false;//用于是否能切换摄像头

    private AVChatOptionalConfig avChatOptionalConfig;//音视频通话配置

    private View root;//主布局View


    public AVChatUI(Context context, View root,AVChatListener listener) {
        this.context = context;
        this.root = root;
        this.aVChatListener = listener;
        this.avChatOptionalConfig = new AVChatOptionalConfig();
        configFromPreference(PreferenceManager.getDefaultSharedPreferences(context));
        updateAVChatOptionalConfig();

    }


    //Config from Preference
    private boolean videoAutoCrop;
    private boolean videoAutoRotate;
    private int videoQuality;
    private boolean serverRecordAudio;
    private boolean serverRecordVideo;
    private boolean defaultFrontCamera;
    private boolean autoCallProximity;
    private int videoHwEncoderMode;
    private int videoHwDecoderMode;
    private boolean videoFpsReported;
    private int audioEffectAecMode;
    private int audioEffectAgcMode;
    private int audioEffectNsMode;
    private int videoMaxBitrate;
    private int deviceDefaultRotation;
    private int deviceRotationOffset;

    private void configFromPreference(SharedPreferences preferences) {
        videoAutoCrop = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_crop_key), true);
        videoAutoRotate = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_rotation_key), true);
        videoQuality = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_quality_key), 0 + ""));
        serverRecordAudio = preferences.getBoolean(context.getString(R.string.nrtc_setting_other_server_record_audio_key), false);
        serverRecordVideo = preferences.getBoolean(context.getString(R.string.nrtc_setting_other_server_record_video_key), false);
        defaultFrontCamera = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_default_front_camera_key), true);
        autoCallProximity = preferences.getBoolean(context.getString(R.string.nrtc_setting_voe_call_proximity_key), true);
        videoHwEncoderMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_hw_encoder_key), 0 + ""));
        videoHwDecoderMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_hw_decoder_key), 0 + ""));
        videoFpsReported = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_fps_reported_key), true);
        audioEffectAecMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_voe_audio_aec_key), 2 + ""));
        audioEffectAgcMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_voe_audio_agc_key), 2 + ""));
        audioEffectNsMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_voe_audio_ns_key), 2 + ""));
        String value1 = preferences.getString(context.getString(R.string.nrtc_setting_vie_max_bitrate_key), 0 + "");
        videoMaxBitrate = Integer.parseInt(TextUtils.isDigitsOnly(value1) && !TextUtils.isEmpty(value1) ? value1 : 0 + "");
        String value2 = preferences.getString(context.getString(R.string.nrtc_setting_other_device_default_rotation_key), 0 + "");
        deviceDefaultRotation = Integer.parseInt(TextUtils.isDigitsOnly(value2) && !TextUtils.isEmpty(value2) ? value2 : 0 + "");
        String value3 = preferences.getString(context.getString(R.string.nrtc_setting_other_device_rotation_fixed_offset_key), 0 + "");
        deviceRotationOffset = Integer.parseInt(TextUtils.isDigitsOnly(value3) && !TextUtils.isEmpty(value3) ? value3 : 0 + "");
    }


    /**
     * 1, autoCallProximity: 语音通话时使用, 距离感应自动黑屏
     * 2, videoAutoCrop: 根据对方屏幕比例在发送前裁剪画面. 双人模式
     * 3, videoAutoRotate: 结合自己设备角度和对方设备角度自动旋转画面
     * 4, serverRecordAudio: 需要服务器录制语音, 同时需要 APP KEY 下面开通了服务器录制功能
     * 5, serverRecordVideo: 需要服务器录制视频, 同时需要 APP KEY 下面开通了服务器录制功能
     * 6, defaultFrontCamera: 默认是否使用前置摄像头
     * 7, videoQuality: 视频质量调整, 最高建议使用480P
     * 8, videoFpsReported: 是否开启视频绘制帧率汇报
     * 9, deviceDefaultRotation: 99.99%情况下你不需要设置这个参数, 当设备固定在水平方向时,并且设备不会移动, 这时是无法确定设备角度的,可以设置一个默认角度
     * 10, deviceRotationOffset: 99.99%情况下你不需要设置这个参数, 当你的设备传感器获取的角度永远偏移固定值时设置,用于修正旋转角度
     * 11, videoMaxBitrate: 视频最大码率设置, 100K ~ 5M. 如果没有特殊需求不要去设置,会影响SDK内部的调节机制
     * 12, audioEffectAecMode: 语音处理选择, 默认使用平台内置,当你发现平台内置不好用时可以设置到SDK内置
     * 13, audioEffectAgcMode: 语音处理选择, 默认使用平台内置,当你发现平台内置不好用时可以设置到SDK内置
     * 14, audioEffectNsMode: 语音处理选择, 默认使用平台内置,当你发现平台内置不好用时可以设置到SDK内置
     * 15, videoHwEncoderMode: 视频编码类型, 默认情况下不用设置.
     * 16, videoHwDecoderMode: 视频解码类型, 默认情况下不用设置.
     */
    private void updateAVChatOptionalConfig() {

        avChatOptionalConfig.enableCallProximity(autoCallProximity)
                .enableVideoCrop(videoAutoCrop)
                .enableVideoRotate(videoAutoRotate)
                .enableServerRecordAudio(serverRecordAudio)
                .enableServerRecordVideo(serverRecordVideo)
                .setDefaultFrontCamera(defaultFrontCamera)
                .setVideoQuality(videoQuality)
                .enableVideoFpsReported(videoFpsReported)
                .setDefaultDeviceRotation(deviceDefaultRotation)
                .setDeviceRotationFixedOffset(deviceRotationOffset);
        if (videoMaxBitrate > 0) {
            avChatOptionalConfig.setVideoMaxBitrate(videoMaxBitrate * 1024);
        }
        switch (audioEffectAecMode) {
            case 0:
                avChatOptionalConfig.setAudioEffectAECMode(AVChatAudioEffectMode.DISABLE);
                break;
            case 1:
                avChatOptionalConfig.setAudioEffectAECMode(AVChatAudioEffectMode.SDK_BUILTIN);
                break;
            case 2:
                avChatOptionalConfig.setAudioEffectAECMode(AVChatAudioEffectMode.PLATFORM_BUILTIN);
                break;
        }
        switch (audioEffectAgcMode) {
            case 0:
                avChatOptionalConfig.setAudioEffectAGCMode(AVChatAudioEffectMode.DISABLE);
                break;
            case 1:
                avChatOptionalConfig.setAudioEffectAGCMode(AVChatAudioEffectMode.SDK_BUILTIN);
                break;
            case 2:
                avChatOptionalConfig.setAudioEffectAGCMode(AVChatAudioEffectMode.PLATFORM_BUILTIN);
                break;
        }
        switch (audioEffectNsMode) {
            case 0:
                avChatOptionalConfig.setAudioEffectNSMode(AVChatAudioEffectMode.DISABLE);
                break;
            case 1:
                avChatOptionalConfig.setAudioEffectNSMode(AVChatAudioEffectMode.SDK_BUILTIN);
                break;
            case 2:
                avChatOptionalConfig.setAudioEffectNSMode(AVChatAudioEffectMode.PLATFORM_BUILTIN);
                break;
        }
        switch (videoHwEncoderMode) {
            case 0:
                avChatOptionalConfig.setVideoEncoderMode(AVChatParameters.MEDIA_CODEC_AUTO);
                break;
            case 1:
                avChatOptionalConfig.setVideoEncoderMode(AVChatParameters.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                avChatOptionalConfig.setVideoEncoderMode(AVChatParameters.MEDIA_CODEC_HARDWARE);
                break;
        }
        switch (videoHwDecoderMode) {
            case 0:
                avChatOptionalConfig.setVideoDecoderMode(AVChatParameters.MEDIA_CODEC_AUTO);
                break;
            case 1:
                avChatOptionalConfig.setVideoDecoderMode(AVChatParameters.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                avChatOptionalConfig.setVideoDecoderMode(AVChatParameters.MEDIA_CODEC_HARDWARE);
                break;
        }

        //观众角色,多人模式下使用. IM Demo没有多人通话, 全部设置为true.
        avChatOptionalConfig.enableAudienceRole(true);
    }


    /**
     * 接听来电
     */
    private void receiveInComingCall() {
        //接听，告知服务器，以便通知其他端

        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {
            onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
        } else {
            onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
        }

        AVChatManager.getInstance().accept(avChatOptionalConfig, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void v) {
                Log.d(TAG,"接听来电成功");

                isCallEstablish.set(true);
                canSwitchCamera = true;
            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(context, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG,"接听音頻电话失败");
                closeSessions(AVChatExitCode.CANCEL);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG,"接听音頻电话异常:" + exception);
            }
        });

        AVChatSoundPlayer.instance(context).stop();
    }


    @Override
    public void onHangUp() {
        Log.d(TAG,"挂断或取消");
    }

    @Override
    public void onRefuse() {
        Log.d(TAG,"拒绝操作");
    }

    @Override
    public void onReceive() {
        Log.d(TAG,"接受开启视频或接听电话");
        //判断当前状态
        switch (callingState) {
            case INCOMING_AUDIO_CALLING://音频通话呼入
                receiveInComingCall();
                onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
                break;
            case AUDIO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_VIDEO_CALLING://视频通话呼入
                receiveInComingCall();
                onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
                break;
            case VIDEO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_AUDIO_TO_VIDEO://音频通话切换到视频通话状态(接受)
                //receiveAudioToVideo();
            default:
                break;
        }
    }

    @Override
    public void toggleMute() {
        Log.d(TAG,"静音");
    }

    @Override
    public void toggleSpeaker() {
        Log.d(TAG,"扩音");
    }

    @Override
    public void toggleRecord() {
        Log.d(TAG,"录制");
    }

    @Override
    public void videoSwitchAudio() {
        Log.d(TAG,"切换到音頻");
    }

    @Override
    public void audioSwitchVideo() {
        Log.d(TAG,"切换到视频");
    }

    @Override
    public void switchCamera() {
        Log.d(TAG,"切换摄像头");
    }

    @Override
    public void closeCamera() {
        Log.d(TAG,"关闭摄像头");
    }

    @Override
    public void receiveSwitchAudioToVideo() {

    }


    //用于回調Activity，用于着装通话界面
    public interface AVChatListener {
        void uiExit();
    }


    /**
     * 关闭本地音视频各项功能
     *
     * @param exitCode 音视频类型
     */
    public void closeSessions(int exitCode) {
        //not  user  hang up active  and warning tone is playing,so wait its end
        Log.d(TAG, "close session -> " + AVChatExitCode.getExitString(exitCode));
       /* if (avChatAudio != null)
            avChatAudio.closeSession(exitCode);
        if (avChatVideo != null)
            avChatVideo.closeSession(exitCode);
        uiHandler.removeCallbacks(runnable);
        showQuitToast(exitCode);
        isCallEstablish.set(false);
        canSwitchCamera = false;
        isClosedCamera = false;*/
        aVChatListener.uiExit();
    }


    /**
     * 状态改变
     *
     * @param stateEnum
     */
    public void onCallStateChange(CallStateEnum stateEnum) {
        callingState = stateEnum;
        //avChatSurface.onCallStateChange(stateEnum);
        avChatAudio.onCallStateChange(stateEnum);
        //avChatVideo.onCallStateChange(stateEnum);
    }

    //获取拔入方或拔出对方的账号
    public String getAccount() {
        if (receiveraccount != null)
            return receiveraccount;
        return null;
    }

    /**
     * 有来电进来的时候调用这个
     */
    public void inComingCalling(AVChatData avChatData) {
        this.avChatData = avChatData;
        receiveraccount = avChatData.getAccount();

        AVChatSoundPlayer.instance(context).play(AVChatSoundPlayer.RingerTypeEnum.RING);

        if (avChatData.getChatType() == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.INCOMING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.INCOMING_VIDEO_CALLING);
        }
    }

    public long getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(long timeBase) {
        this.timeBase = timeBase;
    }
}
