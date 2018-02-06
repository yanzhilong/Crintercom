package com.xmcrtech.intercom;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.xmcrtech.intercom.config.Preferences;
import com.xmcrtech.intercom.login.LoginActivity;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_act);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(canAutoLogin() && !Constant.IsDoor){
                        startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                        finish();
                    }else if(canAutoLogin() && Constant.IsDoor){
                        startActivity(new Intent(WelcomeActivity.this,DoorActivity.class));
                        finish();
                    }
                    else{
                        startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
                        finish();
                    }
                }
            },1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 已经登陆过，自动登陆
     */
    private boolean canAutoLogin() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        Log.i(TAG, "get local sdk token =" + token);
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }
}
