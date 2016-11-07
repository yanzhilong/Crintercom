package com.xmcrtech.intercom.avchat.activity;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoRender;
import com.xmcrtech.intercom.R;

/**
 * Created by yanzl on 16-10-28.
 * 视频绘制类
 */
public class AVChatSurface {

    private static final String TAG = AVChatSurface.class.getSimpleName();
    private Context context;
    private View surfaceRoot;//根布局，用于获取view

    // data
    private String largeAccount; // 显示在大图像的用户id

    private AVChatVideoRender largeRender;

    // view
    private LinearLayout largeSizePreviewLayout;

    public AVChatSurface(Context context, View surfaceRoot) {
        this.context = context;
        this.surfaceRoot = surfaceRoot;
        this.largeRender = new AVChatVideoRender(context);
        initViews();
    }

    private void initViews() {

        if(surfaceRoot != null){
            largeSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.large_size_preview);
        }
    }

    /**
     * 大图像surfaceview 初始化
     * @param account 显示视频的用户id
     */
    public void initLargeSurfaceView(String account){
        Log.d(TAG,"initLargeSurfaceView");
        largeAccount = account;
        /**
         * 获取视频SurfaceView，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        AVChatManager.getInstance().setupVideoRender(account, largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        addIntoLargeSizePreviewLayout(largeRender);

    }

    /**
     * 添加surfaceview到largeSizePreviewLayout
     * @param surfaceView
     */
    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null)
            ((ViewGroup)surfaceView.getParent()).removeView(surfaceView);
        largeSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(false);
    }
}
