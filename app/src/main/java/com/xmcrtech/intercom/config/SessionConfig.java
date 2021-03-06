package com.xmcrtech.intercom.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.xmcrtech.intercom.R;

/**
 * Created by yanzl on 16-10-31.
 * 会话配置
 */
public class SessionConfig {

    private AVChatOptionalConfig avChatOptionalConfig;//音视频通话配置
    private static SessionConfig sessionConfig;
    private Context context;

    private SessionConfig(Context context){
        this.avChatOptionalConfig = new AVChatOptionalConfig();
        this.context = context;
    }

    public static SessionConfig newInstance(Context context){
        if(sessionConfig == null){
            sessionConfig = new SessionConfig(context);
            sessionConfig.updateAVChatOptionalConfig();
        }
        return sessionConfig;
    }

    public AVChatOptionalConfig getAvChatOptionalConfig() {
        return avChatOptionalConfig;
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean videoAutoCrop;
        boolean videoAutoRotate;
        int videoQuality;
        boolean serverRecordAudio;
        boolean serverRecordVideo;
        boolean defaultFrontCamera;
        boolean autoCallProximity;
        int videoHwEncoderMode;
        int videoHwDecoderMode;
        boolean videoFpsReported;
        int audioEffectAecMode;
        int audioEffectAgcMode;
        int audioEffectNsMode;
        int videoMaxBitrate;
        int deviceDefaultRotation;
        int deviceRotationOffset;

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

}
