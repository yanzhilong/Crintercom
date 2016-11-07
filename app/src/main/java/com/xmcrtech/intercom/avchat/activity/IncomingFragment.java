package com.xmcrtech.intercom.avchat.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.constant.CallStateEnum;

/**
 * 有电话呼入的时的界面
 */
public class IncomingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = IncomingFragment.class.getSimpleName();

    public static final String AVCHATLISTENER = "avchatlistener";
    public static final String ACCOUNT = "account";
    public static final String AVCHATTYPE = "AvchatType";//当前通话类型

    //当前通话类型
    AVChatType avChatType;

    private TextView nickname;//备注名称
    private TextView request;//请求类型

    private String account = "";

    public static IncomingFragment newInstance() {
        return new IncomingFragment();
    }

    private AVChatListener avChatUIListener;
    private AVChatActivity avchatActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            avchatActivity = (AVChatActivity) context;
            avChatUIListener = avchatActivity.getAvChatListener();
            avchatActivity.addCallStateChangeListener(new AVChatActivity.CallStateChangeListener() {
                @Override
                public void onCallStateChange(CallStateEnum callStateEnum) {
                    switch (callStateEnum){
                        case ANSWERCONNECTING:
                            request.setText("连接中....");
                            break;
                        case AUDIO:
                        case VIDEO:
                            if(!avchatActivity.isFinishing()){
                                avchatActivity.removeCallStateChangeListener(this);
                            }
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            account = bundle.getString(ACCOUNT);
            avChatType = (AVChatType) bundle.getSerializable(AVCHATTYPE);
            //avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.incoming_frag, container, false);

        nickname = (TextView) root.findViewById(R.id.nickname);
        request = (TextView) root.findViewById(R.id.request);
        root.findViewById(R.id.refuse).setOnClickListener(this);
        root.findViewById(R.id.receive).setOnClickListener(this);

        switch (avChatType){
            case VIDEO:
                request.setText("请求视频聊天");
                break;
            case AUDIO:
                request.setText("请求通话");
                break;
        }

        nickname.setText(account);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refuse://拒绝
                avChatUIListener.refuse();
                break;
            case R.id.receive://接听
                avChatUIListener.answer();
                break;
            default:
                break;
        }
    }
}
