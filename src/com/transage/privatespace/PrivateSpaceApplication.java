package com.transage.privatespace;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
//Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
import android.content.pm.PackageManager;
import com.transage.privatespace.activity.EmptyActivity;
import com.transage.privatespace.bean.AppInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import java.util.ArrayList;
//Transage <zhaoxin>  add  for  privateapp  2017-9-25 end

/**
 * Created by ruipan.dong on 2017/8/21.
 */

public class PrivateSpaceApplication extends Application {

    /**
     * 退出应用
     */
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
    private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();// 数据
    private DatabaseAdapter mDb = null ;
    private PackageManager packageManager = null;
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    public void exitApp(boolean isExitFromRecent) {
        //优化后的逻辑：去掉if条件，只要接收到Home键广播，不管app是在前台还是后台，都执行退出app操作（退出逻辑前后台都可以执行）
        Log.d("PrivateSpaceApplication", "exit privatespace app");
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
        mDb =  new DatabaseAdapter(this);
        packageManager = this.getPackageManager();
        appList.clear();
        appList.addAll(mDb.getApps(packageManager));
        for(AppInfo appinfo : appList)
        {
            if(packageManager.getApplicationEnabledSetting(appinfo.getPackageName())==PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                packageManager.setApplicationEnabledSetting(appinfo.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            }
        }
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
        Intent intent = new Intent(this, EmptyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isExitFromRecent",isExitFromRecent);
        startActivity(intent);
    }

}
