package com.xmcrtech.intercom.avchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.xmcrtech.intercom.R;
import com.xmcrtech.intercom.avchat.AVChatListener;
import com.xmcrtech.intercom.avchat.AVChatSoundPlayer;
import com.xmcrtech.intercom.config.SessionConfig;
import com.xmcrtech.intercom.dialog.WaitDialog;


public class OutGoingFragment extends Fragment implements View.OnClickListener {

    public static final String ACCOUNT = "account";
    public static final String AVCHATLISTENER = "avchatlistener";
    public static final String AVCHATTYPE = "avchattype";//呼叫类型
    private static final String TAG = OutGoingFragment.class.getSimpleName();

    public static OutGoingFragment newInstance() {
        return new OutGoingFragment();
    }

    private AVChatActivity avchatActivity;

    private String account = "";
    private AVChatListener avChatUIListener;
    private AVChatType avChatType;
    private WaitDialog waitDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity){
            avchatActivity = (AVChatActivity) context;
            avChatUIListener = avchatActivity.getAvChatListener();
        }
    }

    private DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            avChatUIListener.onHangUp();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            account = bundle.getString(ACCOUNT);
            //avChatUIListener = (AVChatListener) bundle.getSerializable(AVCHATLISTENER);
            avChatType = (AVChatType) bundle.getSerializable(AVCHATTYPE);
        }
        waitDialog = new WaitDialog(this.getContext(), getString(R.string.connectioning), true,onCancelListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.outgoing_frag, container, false);

        root.findViewById(R.id.cancel).setOnClickListener(this);
        //如果有设置菜单，需要加这个
        setHasOptionsMenu(true);

        outGoingCalling(account,avChatType);
        return root;
    }

    /**
     * 拨打音视频
     */
    public void outGoingCalling(String account, AVChatType callTypeEnum) {

        waitDialog.show();

        AVChatSoundPlayer.instance(this.getContext()).play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);

        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = "extra_data";

        AVChatManager.getInstance().call(account, callTypeEnum, SessionConfig.newInstance(getContext()).getAvChatOptionalConfig(),
                notifyOption, new AVChatCallback<AVChatData>() {
                    @Override
                    public void onSuccess(AVChatData data) {
                        waitDialog.dismiss();
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.d(TAG, "avChat call failed code->" + code);
                        waitDialog.dismiss();

                        AVChatSoundPlayer.instance(getContext()).stop();

                        if (code == ResponseCode.RES_FORBIDDEN) {
                            Toast.makeText(getContext(), R.string.avchat_no_permission, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                        }
                        avChatUIListener.onHangUp();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Log.d(TAG, "avChat call onException->" + exception);
                        waitDialog.dismiss();
                        Toast.makeText(getContext(), R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                        avChatUIListener.onHangUp();
                    }
                });
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
            case R.id.cancel:
                avChatUIListener.onHangUp();
                break;
            default:
                break;
        }
    }



}
