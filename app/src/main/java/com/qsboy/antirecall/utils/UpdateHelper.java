/*
 * Copyright © 2016 - 2018 by GitHub.com/JasonQS
 * anti-recall.qsboy.com
 * All Rights Reserved
 */

package com.qsboy.antirecall.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.gson.Gson;
import com.qsboy.antirecall.bean.UpdateInfo;

import java.io.File;

public class UpdateHelper {

    private static final String REQUEST_URL = "http://anti-recall.qsboy.com/version.json";
    private String TAG = "Check Update";
    private Entry entry = new Entry();
    private UpdateInfo updateInfo;
    private Activity activity;
    private CheckUpdateListener checkUpdateListener;

    public UpdateHelper(Activity activity) {
        this.activity = activity;
    }

    public void checkUpdate() {
        String apkPath = activity.getExternalFilesDir("") + File.separator;
        Log.e(TAG, "checkUpdateListener: apk path: " + apkPath);
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(REQUEST_URL)
                .request(new RequestVersionListener() {

                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(DownloadBuilder downloadBuilder, String result) {
//                        entry = new Gson().fromJson(result, Entry.class);
                        updateInfo = new Gson().fromJson(result, UpdateInfo.class);
                        Log.d(TAG, "onRequestVersionSuccess: " + entry);

                        boolean needUpdate = needUpdate(entry.versionCode);
                        if (checkUpdateListener != null)
                            checkUpdateListener.needUpdate(needUpdate, entry.versionName);
                        if (needUpdate)
                            return UIData
                                    .create()
                                    .setTitle(updateInfo.getUpdateDate())
                                    .setContent(updateInfo.getUpdateContent())
                                    .setDownloadUrl(entry.path);
                        else return null;
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        Log.i(TAG, "onRequestVersionFailure: " + message);
                        if (checkUpdateListener != null)
                            checkUpdateListener.error();
                    }
                })
                .setForceUpdateListener(() -> activity.finish())
                .setDownloadAPKPath(apkPath).executeMission(activity);

    }

    private boolean needUpdate(int versionCode) {
        int localVersion = 1;
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        boolean needUpdate = versionCode > localVersion;
        Log.d(TAG, "local version code : " + localVersion);
        Log.d(TAG, "server version code : " + versionCode);
        Log.d(TAG, "need update?    " + needUpdate);

        return needUpdate;
    }

    public void setCheckUpdateListener(CheckUpdateListener checkUpdateListener) {
        this.checkUpdateListener = checkUpdateListener;
    }

    public interface CheckUpdateListener {
        void needUpdate(boolean needUpdate, String versionName);

        void error();
    }


    private class Entry {

        /**
         * versionCode : 6
         * versionName : v5.1.0
         * desc : v5.1.0-release
         * force : true
         * path : http://cdn.qsboy.com/anti-recall/releases/anti-recall-v5.1.0.apk
         */

        int versionCode;
        String versionName;
        String title;
        String desc;
        boolean force;
        String path;

        @Override
        public String toString() {
            return "\nversionCode: " + versionCode +
                    "\nversionName: " + versionName +
                    "\ndesc: " + desc +
                    "\npath: " + path +
                    "\nforce: " + force;
        }
    }
}
