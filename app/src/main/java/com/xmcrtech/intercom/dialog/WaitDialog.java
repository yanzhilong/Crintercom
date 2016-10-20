package com.xmcrtech.intercom.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xmcrtech.intercom.R;


public class WaitDialog extends Dialog {

    private Context mContext;

    private TextView tv_message = null;

    private String message = null;

    public WaitDialog(Activity context) {
        this(context,null);
    }

    public WaitDialog(Activity context, String message) {
        this(context,message,false,null);
    }

    public WaitDialog(Context context, String message, boolean cancelable, OnCancelListener cancelListener) {
        super(context, R.style.easy_dialog_style);
        this.setCancelable(cancelable);
        this.setOnCancelListener(cancelListener);
        mContext = context;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_dialog);
        tv_message = (TextView) findViewById(R.id.tv_message);

        if (message != null && !message.isEmpty()) {
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText(message);
        } else {
            tv_message.setVisibility(View.GONE);
        }
    }

    public void showMessage(){
        if (message != null && !TextUtils.isEmpty(message)) {
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText(message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        showMessage();
    }

    @Override
    public void show() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.show();
            } catch (Exception e) {

            }
        }

    }

    @Override
    public void dismiss() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.dismiss();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void cancel() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.cancel();
            } catch (Exception e) {
            }
        }
    }
}
