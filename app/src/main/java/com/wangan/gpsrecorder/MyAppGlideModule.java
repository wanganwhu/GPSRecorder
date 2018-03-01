package com.wangan.gpsrecorder;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by 10394 on 2018-03-01.
 */

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int memoryCacheSizeBytes = 1024 * 1024 * 60; // 40mb
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setBitmapPoolScreens(10)
                .build();
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        builder.setBitmapPool(new LruBitmapPool(calculator.getBitmapPoolSize()));
    }

}
