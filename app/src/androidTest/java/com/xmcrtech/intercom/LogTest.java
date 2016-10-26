package com.xmcrtech.intercom;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.xmcrtech.intercom.util.AndroidFileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;


/**
 * Created by yanzl on 16-10-2.
 */

@RunWith(AndroidJUnit4.class)
public class LogTest {

    private static final String TAG = LogTest.class.getSimpleName();
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void cleanUp() {
        //mLocalDataSource.deleteAllSentences();
    }

    @Test
    public void getLogTest() {
        File file = AndroidFileUtils.newInstance(context).getFile("/" + context.getPackageName() + "/nim/log/nim_sdk.log");
        String path = file.getPath();
        Log.d(TAG,"Log目录:" + path);
    }


}
