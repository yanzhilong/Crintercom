package com.xmcrtech.intercom.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by yanzl on 16-10-26.
 */
public class AndroidFileUtils {

    private Context context;
    String basePath;
    File externalStorage;
    private static AndroidFileUtils androidFileUtils;

    public AndroidFileUtils(Context context) {
        this.context = context;
        basePath = context.getFilesDir().getAbsolutePath();
        externalStorage = Environment.getExternalStorageDirectory();
    }

    public static synchronized AndroidFileUtils newInstance(Context context) {

        if(androidFileUtils == null){
            androidFileUtils = new AndroidFileUtils(context);
        }
        return androidFileUtils;
    }

    /**
     * 根据文件或文件平夹返回File
     * @param filePath
     * @return
     */
    public File getFile(String filePath){
        File file = new File(externalStorage + "/" + filePath);
        return file;
    }

    /**
     * 根据文件或文件平夹返回File
     * @param filePath
     * @return
     */
    public File getExternalFile(String filePath){
        File file = new File(basePath + "/" + filePath);
        return file;
    }
}
