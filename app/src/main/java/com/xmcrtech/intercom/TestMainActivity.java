package com.xmcrtech.intercom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xmcrtech.intercom.login.LoginActivity;

public class TestMainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textmain_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        //返回按钮
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                //登陆
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.register:
                //注册

                break;
            case R.id.dialing:
                //拔号

                break;
            case R.id.avchat:
                //通话相关

                break;
            default:
                break;
        }
    }
}
