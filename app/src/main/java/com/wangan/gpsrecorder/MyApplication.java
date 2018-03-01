package com.wangan.gpsrecorder;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by 10394 on 2018-02-28.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(this);
    }

}
