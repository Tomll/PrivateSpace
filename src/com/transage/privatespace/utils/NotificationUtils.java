package com.transage.privatespace.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.widget.RemoteViews;

import com.transage.privatespace.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知管理工具类
 * Created by yanjie.xu on 2017/9/27.
 */
public class NotificationUtils {
    public static final int ADD_CONTACT_NOTITY_ID = 141;
    public static final int REMOVE_CONTACT_NOTITY_ID = 142;

    private Context mContext;
    // NotificationManager ： 是状态栏通知的管理类，负责发通知、清楚通知等。
    private NotificationManager manager;
    private static NotificationUtils mNotify;
    private int mAddCounts = 0;
    private int mRemoveCounts = 0;
    private boolean mIsShow = false;
    // 定义Map来保存Notification对象
    private Map<Integer, Notification> map = null;

    private NotificationUtils(Context context) {
        this.mContext = context;
        // NotificationManager 是一个系统Service，必须通过 getSystemService()方法来获取。
        manager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        map = new HashMap<>();
    }

    public static NotificationUtils getInstance(Context context) {
        if (mNotify == null) {
            mNotify = new NotificationUtils(context);
        }
        return mNotify;
    }

    public void showNotification(int notificationId, int counts) {
        mIsShow = true;
        // 判断对应id的Notification是否已经显示， 以免同一个Notification出现多次
        if (!map.containsKey(notificationId)) {
            // 创建通知对象
            Notification notification = new Notification();
            // 设置通知栏滚动显示文字
            notification.tickerText = this.mContext.getString(R.string.title);
            // 设置显示时间
            notification.when = System.currentTimeMillis();
            // 设置通知显示的图标
            notification.icon = R.mipmap.ic_launcher;
            // 设置通知的特性: 通知被点击后，自动消失
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            // 设置点击通知栏操作
//            Intent in = new Intent(mContext, MainActivity.class);// 点击跳转到指定页面
//            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, in,
//                    0);
//            notification.contentIntent = pIntent;
//            // 设置通知的显示视图
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.notification_contentview);
//            // 设置暂停按钮的点击事件
//            Intent pause = new Intent(mContext, MainActivity.class);// 设置跳转到对应界面
//            PendingIntent pauseIn = PendingIntent.getActivity(mContext, 0, in,
//                    0);
//            // 这里可以通过Bundle等传递参数
//            remoteViews.setOnClickPendingIntent(R.id.pause, pauseIn);
//            /********** 简单分隔 **************************/
//            // 设置取消按钮的点击事件
//            Intent stop = new Intent(mContext, MainActivity.class);// 设置跳转到对应界面
//            PendingIntent stopIn = PendingIntent
//                    .getActivity(mContext, 0, in, 0);
//            // 这里可以通过Bundle等传递参数
//            remoteViews.setOnClickPendingIntent(R.id.cancel, stopIn);
            int titleRes = 0;
            if (notificationId == ADD_CONTACT_NOTITY_ID) {
                mAddCounts = counts;
                titleRes = R.string.notification_title_add;
            }
            if (notificationId == REMOVE_CONTACT_NOTITY_ID) {
                mRemoveCounts = counts;
                titleRes = R.string.notification_title_remove;
            }
            if (titleRes > 0) {
                remoteViews.setTextViewText(R.id.notifycation_contentview_title,
                        mContext.getResources().getText(titleRes));
            }

            remoteViews.setTextViewText(R.id.notifycation_contentview_process, "0 / " + counts);
            // 设置通知的显示视图
            notification.contentView = remoteViews;

            // 发出通知
            manager.notify(notificationId, notification);
            map.put(notificationId, notification);// 存入Map中
        }
    }

    /**
     * 取消通知操作
     *
     * @param notificationId
     */
    public void cancel(int notificationId) {
        mAddCounts = 0;
        mRemoveCounts = 0;
        manager.cancel(notificationId);
        map.remove(notificationId);
        mIsShow = false;
    }

    public boolean isShow() {
        return mIsShow;
    }

    public void updateProgress(int notificationId, int progress) {
        Notification notify = map.get(notificationId);
        String lableText = "";
        int count = 0;
        boolean isCancle = false;
        if (null != notify) {
            if (notificationId == ADD_CONTACT_NOTITY_ID) {
                count = mAddCounts;
                lableText = progress + " / " + mAddCounts;
                isCancle = (progress == mAddCounts);
            }
            if (notificationId == REMOVE_CONTACT_NOTITY_ID) {
                count = mRemoveCounts;
                lableText = progress + " / " + mRemoveCounts;
//                progressPer = (progress / mRemoveCounts) * 100;
                isCancle = ((progress) == mRemoveCounts);
            }
            // 修改进度条
            notify.contentView.setProgressBar(R.id.pBar, count, progress, false);
            notify.contentView.setTextViewText(R.id.notifycation_contentview_process, lableText);
            manager.notify(notificationId, notify);

            if (isCancle) {
                cancel(notificationId);
            }
        }
    }
}