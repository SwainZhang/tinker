package com.emery.test.tinker;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import FiexUtils.FixUtil;


/**
 * Created by MyPC on 2016/12/14.
 */

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        FixUtil.loadDexFile(base);
        super.attachBaseContext(base);
    }
}
