package com.transage.privatespace.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.transage.privatespace.PrivateSpaceApplication;

/**
 * Created by ruipan.dong on 2017/8/18.
 */

public class BaseActivity extends AppCompatActivity {

    private SystemKeyEventReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new SystemKeyEventReceiver();
        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    //注册广播
    public void registerReceiver() {
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Log.d("BaseActivity", "registerReceiver");
    }

    //反注册广播
    public void unRegisterReceiver() {
        unregisterReceiver(receiver);
        Log.d("BaseActivity", "unRegisterReceiver");
    }


    /**
     * 系统按键事件广播接收器
     */
    private class SystemKeyEventReceiver extends BroadcastReceiver {

        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason == null)
                    return;
                // Home键
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) && Login.sp.getBoolean("fastExit", false)) {
                    ((PrivateSpaceApplication) getApplication()).exitApp(false);
                }
                // 最近任务列表键
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS) && Login.sp.getBoolean("fastExit", false)) {
                    ((PrivateSpaceApplication) getApplication()).exitApp(true);
                }
            }
        }
    }

}
