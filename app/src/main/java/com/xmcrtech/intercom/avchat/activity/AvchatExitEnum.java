package com.xmcrtech.intercom.avchat.activity;

/**
 * Created by yanzl on 16-11-1.
 */
public enum AvchatExitEnum {

    OTHER(""),//其它，不提示
    LOCALDEVICEFAIL("本地音视频启动失败"),
    CONNECTIONFAIL("建立连接失败"),
    UNKNOWEXCEPTION("未知异常"),
    HANGUP("通话结束"),
    NORESPONSE("对方无应答");


    private String message;

    AvchatExitEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
