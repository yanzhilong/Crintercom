package com.xmcrtech.intercom.avchat.activity;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoRender;
import com.xmcrtech.intercom.R;

/**
 * Created by yanzl on 16-10-28.
 * 视频绘制类
 */
public class AVChatSurface implements View.OnClickListener {

    private static final String TAG = AVChatSurface.class.getSimpleName();
    private Context context;
    private View surfaceRoot;//根布局，用于获取view

    private boolean localPreviewInSmallSize = true;
    private boolean isPeerVideoOff = false;
    private boolean isLocalVideoOff = false;

    // data
    private String largeAccount; // 显示在大图像的用户id
    private String smallAccount; // 显示在小图像的用户id

    //render
    private AVChatVideoRender smallRender;
    private AVChatVideoRender largeRender;

    // view
    private LinearLayout largeSizePreviewLayout;
    private LinearLayout smallSizePreviewLayout;
    private ImageView smallSizePreviewCoverImg;//stands for peer or local close camera
    private TextView largeSizePreviewCoverLayout;//stands for peer or local close camera

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
            largeSizePreviewCoverLayout = (TextView) surfaceRoot.findViewById(R.id.notificationLayout);

            largeSizePreviewLayout.setOnClickListener(this);
        }
    }


    /**
     * 大小图像显示切换
     * @param user1 用户1的account
     * @param user2 用户2的account
     */
    private void switchRender(String user1, String user2) {

        //先取消用户的画布
        AVChatManager.getInstance().setupVideoRender(user1, null, false, 0);
        AVChatManager.getInstance().setupVideoRender(user2, null, false, 0);

        //交换画布
        //如果存在多个用户,建议用Map维护account,render关系.
        //目前只有两个用户,并且认为这两个account肯定是对的
        AVChatVideoRender render1;
        AVChatVideoRender render2;
        if(user1.equals(smallAccount)) {
            render1 = largeRender;
            render2 = smallRender;
        } else {
            render1 = smallRender;
            render2 = largeRender;
        }

        //重新设置上画布
        AVChatManager.getInstance().setupVideoRender(user1, render1, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        AVChatManager.getInstance().setupVideoRender(user2, render2, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
    }


    /**
     * 对方打开了摄像头
     */
    public void peerVideoOn() {
        isPeerVideoOff = false;
        if (localPreviewInSmallSize) {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        } else {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        }
    }

    /**
     * 对方关闭了摄像头
     */
    public void peerVideoOff(){
        isPeerVideoOff = true;
        if(localPreviewInSmallSize){ //local preview in small size layout, then peer preview should in large size layout
            largeSizePreviewCoverLayout.setText("对方关闭了摄像头");
            largeSizePreviewCoverLayout.setVisibility(View.VISIBLE);
        }else{  // peer preview in small size layout
            smallSizePreviewCoverImg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 本地打开了摄像头
     */
    public void localVideoOn() {
        isLocalVideoOff = false;
        if (localPreviewInSmallSize) {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        } else {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 本地关闭了摄像头
     */
    public void localVideoOff(){
        isLocalVideoOff = true;
        if(localPreviewInSmallSize)
            smallSizePreviewCoverImg.setVisibility(View.VISIBLE);
        else{
            largeSizePreviewCoverLayout.setText("本地关闭了摄像头");
            largeSizePreviewCoverLayout.setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.large_size_preview:
                String temp;
                switchRender(smallAccount, largeAccount);//切换摄像头
                temp = largeAccount;
                largeAccount = smallAccount;
                smallAccount = temp;
                switchAndSetLayout();
                break;
        }
    }

    /**
     * 摄像头切换时，布局显隐
     */
    private void switchAndSetLayout() {
        localPreviewInSmallSize = !localPreviewInSmallSize;
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if(isPeerVideoOff) {
            peerVideoOff();
        }
        if(isLocalVideoOff) {
            localVideoOff();
        }
    }
}
