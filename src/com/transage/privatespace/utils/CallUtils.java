package com.transage.privatespace.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import com.transage.privatespace.R;

/**
 * Created by yanjie.xu on 2017/10/17.
 */

public class CallUtils {
    /**
     * 方法：打电话 或 发短信
     */
    public static void callPhoneOrSendSms(Context context, int type, String phoneNum) {
        Intent intent = null;
        if (type == 1) { // 打电话
            // 检查并申请CALL_PHONE权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE},
                        1);// 申请打电话权限
                return;
            }
            intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        } else if (type == 2) { // 发短信
            // 检查并申请SEND_SMS权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, 1);// 申请发短信权限
                return;
            }
            intent = new Intent(android.content.Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNum));
        }
        context.startActivity(intent);
    }

    /**
     * 发送邮件
     */
    public static void sendEmail(Context context, String[] emails) {
        //系统邮件系统的动作为android.content.Intent.ACTION_SEND
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
//        emailReciver = new String[]{"pop1030123@163.com", "fulon@163.com"};
//        emailSubject = "你有一条短信";
//        emailBody = sb.toString();

        //设置邮件默认地址
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emails);
        //设置邮件默认标题
//        email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
        //设置要默认发送的内容
//        email.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
        //调用系统的邮件系统
        String title = context.getResources().getString(R.string.send_email_title);
        context.startActivity(Intent.createChooser(emailIntent, title));
    }
}
