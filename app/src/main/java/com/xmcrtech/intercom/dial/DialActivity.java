package com.xmcrtech.intercom.dial;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.activity.AVChatActivity;

public class DialActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String OBJECT = "object";
    private static final String TAG = DialActivity.class.getSimpleName();

    private EditText account;
    private String peeraccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dial_act);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        //返回按钮
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        //标题
        ab.setTitle(R.string.title_dial);

        account = (EditText) findViewById(R.id.account);

        findViewById(R.id.audiocall).setOnClickListener(this);
        findViewById(R.id.videocall).setOnClickListener(this);

        /*Fragment fragment = null;

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contentFrame, fragment)
                    .commit();
        }*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.audiocall:
                peeraccount = account.getText().toString().trim();
                AVChatActivity.outgoing(this,peeraccount,AVChatType.AUDIO);
                break;
            case R.id.videocall:
                peeraccount = account.getText().toString().trim();
                AVChatActivity.outgoing(this,peeraccount,AVChatType.VIDEO);
                break;
        }
    }
}

