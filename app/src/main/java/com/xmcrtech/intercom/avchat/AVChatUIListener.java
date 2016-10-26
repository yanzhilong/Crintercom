package com.xmcrtech.intercom.avchat;

/**
 * 音视频界面操作
 */
public interface AVChatUIListener {
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
}
