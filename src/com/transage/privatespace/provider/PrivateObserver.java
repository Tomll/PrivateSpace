package com.transage.privatespace.provider;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.transage.privatespace.loader.PrivateSpaceLoader;

/**
 * 监听数据库变化
 *
 * Created by yanjie.xu on 2017/7/27.
 */

public class PrivateObserver extends ContentObserver {
    private static final String TAG= "PrivateObserver";

    private Handler mHandler;

    public PrivateObserver(Handler handler) {
        super(handler);
        this.mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (uri != null) {
            switch (PrivateProvider.uriMatcher.match(uri)) {
                case PrivateProvider.MATCH_PHONE:
                    //查询手机号码
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(PrivateSpaceLoader.REFRESHE_CONTACT);
                        Log.i(TAG, "onChange selfChange MATCH_PHONE" + selfChange);
                    }
                    break;
                case PrivateProvider.MATCH_SMS:
                    //查询短信
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(PrivateSpaceLoader.REFRESHE_SMS);
                        Log.i(TAG, "onChange selfChange MATCH_SMS" + selfChange);
                    }
                    break;
                case PrivateProvider.MATCH_CALL:
                    //查询通话记录
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(PrivateSpaceLoader.REFRESHE_CALLRECORD);
                        Log.i(TAG, "onChange selfChange MATCH_CALL" + selfChange);
                    }
                    break;
            }

        }
    }
}
