package com.xmcrtech.intercom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.xmcrtech.intercom.config.Preferences;
import com.xmcrtech.intercom.login.LoginActivity;
import com.xmcrtech.intercom.util.ActivityUtils;

/**
 * 登陆后的第一个界面
 * 1. 加载一个MainFragment用于呼叫
 * 2. 输入门牌号码
 * 3. 注销账号
 *
 */
public class DoorActivity extends AppCompatActivity{

    private static final String EXTRA_APP_QUIT = "APP_QUIT";

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, DoorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }


    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏
        /* requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */

//        Window window = getWindow();
//        //隐藏标题栏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //隐藏状态栏
//        //定义全屏参数
//        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        //设置当前窗体为全屏显示
//        window.setFlags(flag, flag);

        setContentView(R.layout.door_act);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar ab = getSupportActionBar();
//        //返回按钮
//        ab.setDisplayHomeAsUpEnabled(false);
//        ab.setDisplayShowHomeEnabled(false);
//        //标题
//        ab.setTitle("叮咚");



        MainFragment mainfragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        DialerFragment doorfragment = (DialerFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (doorfragment == null && Constant.IsDoor) {
            doorfragment = DialerFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), doorfragment, R.id.contentFrame);
        }

        if (mainfragment == null && !Constant.IsDoor) {
            mainfragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mainfragment, R.id.contentFrame);
        }
    }

    private void onParseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        }
    }

    // 注销
    private void onLogout() {

        NIMClient.getService(AuthService.class).logout();
        Preferences.saveUserToken("");
        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(true);
    }

}
