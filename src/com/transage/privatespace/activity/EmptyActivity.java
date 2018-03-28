package com.transage.privatespace.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ruipan.dong on 2017/8/21.
 */

public class EmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        //为什么要加此逻辑？
        //因为：进入私密-->通过菜单栏进入setting-->按下recent键后，没有正确进入RecentActivity,而是看到setting界面直接关掉了
        //原因：按下recent后，系统立刻执行了 go to RecentActivity的逻辑，然后私密收到了recent广播，执行退出逻辑：清空栈，并跳
        //到EmptyActivity并finish，从RecentActivity到EmptyActivity然后finish 这个过程很快（有时几乎看不到界面的变化），所
        //以我们看到现象是，按recent键之后，setting界面直接关掉了
        //处理办法：执行完退出逻辑后，再启动一下RecentsActivity界面
        if (getIntent().getBooleanExtra("isExitFromRecent", false)){
            Intent intent2 = new Intent();
            ComponentName name = new ComponentName("com.android.systemui", "com.android.systemui.recents.RecentsActivity");
            intent2.setComponent(name);
            startActivity(intent2);
        }
    }
}
