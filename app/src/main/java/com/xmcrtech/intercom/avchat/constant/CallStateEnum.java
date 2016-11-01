package com.xmcrtech.intercom.avchat.constant;

/**
 * 呼叫状态,用来控制刷新界面
 * Created by hzxuwen on 2015/4/27.
 */
public enum CallStateEnum {
    INVALID(-1), //无效的状态,该状态下无界面显示

    VIDEO(0), //正在进行视频通话(发起者)
    OUTGOING_VIDEO_CALLING(2), //邀请好友视频通话
    INCOMING_VIDEO_CALLING(4), //来自好友的视频通话邀请
    OUTGOING_AUDIO_TO_VIDEO(6), //向好友发起从语音切换到视频的邀请
    OUTGOING_AUDIO_TO_VIDEO_FAIL(61), //向好友发起从语音切换到视频的邀请失败
    OUTGOING_AUDIO_TO_VIDEO_REJECT(62), //向好友发起从语音切换到视频的邀请被拒绝

    OUTGOING_VIDEO_TO_AUDIO_FAIL(63), //向好友发起从视频切换到语音的邀请失败

    VIDEO_CONNECTING(8), //视频通话连接中
    VIDEO_OFF(10), // 对方关闭摄像头

    AUDIO(1), //正在进行语音通话(发起者)
    OUTGOING_AUDIO_CALLING(3), //邀请好友语音通话
    INCOMING_AUDIO_CALLING(5), //来自好友的语音通话邀请
    INCOMING_AUDIO_TO_VIDEO(7), //音频切换为视频的邀请
    RECEIVE_AUDIO_TO_VIDEO_FAIL(71), //同意音频切换为视频的邀请失败
    AUDIO_CONNECTING(9), //语音通话连接中

    INCOMING(10),//正在呼入
    OUTGOING(11),//正在呼出

    ANSWERCONNECTING(16),

    AUDIOINCOMING(12),//请求音频通话
    VIDEOINCOMING(13),//请求视频通话
    AUDIOOUTGOING(14),//呼叫音频通话
    VIDEOOUTGOING(15);//呼叫视频通话

    private int value;

    CallStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isVideoMode(CallStateEnum value) {
        return value.getValue() % 2 == 0;
    }

    public static boolean isAudioMode(CallStateEnum value) {
        return value.getValue() % 2 == 1;
    }

    public static CallStateEnum getCallStateEnum(int value) {
        for (CallStateEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }

        return INVALID;
    }
}
