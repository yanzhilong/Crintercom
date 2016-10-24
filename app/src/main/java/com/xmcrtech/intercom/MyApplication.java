package com.xmcrtech.intercom;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.xmcrtech.intercom.avchat.activity.AVChatActivity;
import com.xmcrtech.intercom.config.Preferences;
import com.xmcrtech.intercom.util.ScreenUtil;
import com.xmcrtech.intercom.util.SystemUtil;


/**
 * Created by yanzl on 16-10-17.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Constant.setContext(this);

        // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）
        NIMClient.init(this, loginInfo(), options());

        if (inMainProcess()) {

            // 注册网络通话来电
            enableAVChat();

        }
    }

    private SDKOptions options() {
        SDKOptions options = new SDKOptions();

        // 配置保存图片，文件，log等数据的目录
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置数据库加密秘钥
        options.databaseEncryptKey = "NETEASE";

        // 配置是否需要预下载附件缩略图
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，
        options.thumbnailSize = (int) (165.0 / 320.0 * ScreenUtil.screenWidth);

        return options;
    }

    private LoginInfo loginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            Constant.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }


    public boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    /**
     * 音视频通话配置与监听
     */
    private void enableAVChat() {
        registerAVChatIncomingCallObserver(true);
    }

    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                String extra = data.getExtra();
                Log.e("Extra", "Extra Message->" + extra);
                // 有网络来电打开AVChatActivity
                AVChatActivity.incoming(Constant.getContext(), data);
            }
        }, register);
    }

}
