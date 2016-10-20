package com.xmcrtech.intercom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.xmcrtech.intercom.config.Preferences;
import com.xmcrtech.intercom.login.LoginActivity;


public class MainFragment extends Fragment implements View.OnClickListener {

    public static final String OBJECT = "object";
    private static final String TAG = MainFragment.class.getSimpleName();
    private Object object;
    private TextView userinfoTv;
    private Button loginout;

    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(OBJECT)) {
            object = getArguments().getSerializable(OBJECT);
        }
        registerObservers(true);



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.main_frag, container, false);

        userinfoTv = (TextView) root.findViewById(R.id.userinfo);
        loginout = (Button) root.findViewById(R.id.loginout);

        loginout.setOnClickListener(this);
        //如果有设置菜单，需要加这个
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    /**
     * 注册或取消注册监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    /**
     * 用户状态变化
     */
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                kickOut(code);
            } else {
                if (code == StatusCode.NET_BROKEN) {
                    userinfoTv.setText("网絡错误");
                } else if (code == StatusCode.UNLOGIN) {
                    userinfoTv.setText("未登陆");
                } else if (code == StatusCode.CONNECTING) {

                } else if (code == StatusCode.LOGINING) {
                    userinfoTv.setText("登陆中");
                } else if(code == StatusCode.LOGINED){
                    userinfoTv.setText("登陆成功");
                }
            }
        }
    };

    /**
     * 被踢下线了，或密码被修改了....
     * @param code
     */
    private void kickOut(StatusCode code) {
        Preferences.saveUserToken("");

        if (code == StatusCode.PWD_ERROR) {
            Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
        LoginActivity.start(getActivity(), true);
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginout:
                MainActivity.logout(this.getContext(),false);

            default:
                break;
        }
    }
}
