package com.xmcrtech.intercom;

import android.content.Context;

/**
 * Created by yanzl on 16-10-17.
 */
public class Constant {

    public static final String SPNAME = "intercom";//SharedPreferences名字
    private static Context context;
    private static String account;//账号
    public static boolean IsDoor = true;



    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Constant.context = context;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        Constant.account = account;
    }
}
