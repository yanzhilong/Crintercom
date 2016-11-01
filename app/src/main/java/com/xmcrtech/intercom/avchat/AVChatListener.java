package com.xmcrtech.intercom.avchat;

import java.io.Serializable;

/**
 * 音视频界面操作
 */
public interface AVChatListener extends Serializable{
    void onHangUp();//挂断或取消
    void onRefuse();//拒绝操作，根据当前状态来选择合适的操作
    void onReceive();//接受 开启操作，根据当前状态来选择合适的操作
    void toggleMute();//静音
    void toggleSpeaker();//扩音
    void toggleRecord();//录制
    void videoSwitchAudio();//切换到音頻
    void audioSwitchVideo();//切换到视频
    void switchCamera();//切换摄像头
    void closeCamera();//关闭摄像头


    void refuseaudioSwitchVideo();//拒绝音频切换到视频
    void micMute();//静音
    void micOpen();//开启
    void speakerMute();//扩音关闭
    void speakerOpen();//扩音开启
    void refuse();//拒绝接听
    void answer();//接听来电
    void receiveSwitchAudioToVideo();//同意音频切换到视频


}
