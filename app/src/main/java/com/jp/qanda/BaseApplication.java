package com.jp.qanda;

import android.app.Application;
import android.content.Context;

import com.jp.qanda.util.AudioHandleUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @author jpwang
 * @since 6/8/16
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AudioHandleUtil.init(20, this.getApplicationContext());
        initImageLoader(this.getApplicationContext());
    }

    private void initImageLoader(Context context) {
        if (ImageLoader.getInstance().isInited()) {
            return;
        }
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        if (BuildConfig.DEBUG) {
            config.writeDebugLogs(); // Remove for release app
        }

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
}
