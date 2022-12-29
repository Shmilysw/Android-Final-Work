package com.example.forecast.base;

import android.app.Application;

import com.example.forecast.db.DBManager;

import org.xutils.x;

public class UniteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        // 初始化数据库, 项目工程被创建，数据库也同时被创建
        DBManager.initBD(this);
    }
}
