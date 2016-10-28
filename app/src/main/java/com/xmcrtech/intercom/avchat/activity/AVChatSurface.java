package com.xmcrtech.intercom.avchat.activity;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private String smallAccount; // 显示在小图像的用户id

    //render
    private AVChatVideoRender smallRender;
    private AVChatVideoRender largeRender;

    // view
    private LinearLayout largeSizePreviewLayout;
    private FrameLayout smallSizePreviewFrameLayout;
    private LinearLayout smallSizePreviewLayout;
    private ImageView smallSizePreviewCoverImg;//stands for peer or local close camera
    private View largeSizePreviewCoverLayout;//stands for peer or local close camera

    public AVChatSurface(Context context, View surfaceRoot) {
        this.context = context;
        this.surfaceRoot = surfaceRoot;
        this.smallRender = new AVChatVideoRender(context);
        this.largeRender = new AVChatVideoRender(context);
        initViews();
    }

    private void initViews() {

        if(surfaceRoot != null){

            smallSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.small_size_preview);
            smallSizePreviewCoverImg = (ImageView) surfaceRoot.findViewById(R.id.smallSizePreviewCoverImg);
            largeSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.large_size_preview);
            largeSizePreviewCoverLayout = surfaceRoot.findViewById(R.id.notificationLayout);

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
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
    }


    /**
     * 小图像surfaceview 初始化
     * @param account
     * @return
     */
    public void initSmallSurfaceView(String account){
        smallAccount = account;

        /**
         * 获取视频SurfaceView，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        AVChatManager.getInstance().setupVideoRender(account, smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        addIntoSmallSizePreviewLayout(smallRender);

    }

    /**
     * 添加surfaceview到smallSizePreviewLayout
     */
    private void addIntoSmallSizePreviewLayout(SurfaceView surfaceView) {
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (surfaceView.getParent() != null) {
            ((ViewGroup)surfaceView.getParent()).removeView(surfaceView);
        }
        smallSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        smallSizePreviewLayout.setVisibility(View.VISIBLE);
    }
}
