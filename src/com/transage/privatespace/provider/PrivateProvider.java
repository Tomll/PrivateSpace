package com.transage.privatespace.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.utils.ImportExportUtils;

/**
 * Created by yanjie.xu on 2017/7/24.
 *
 * used in "com.android.server.telecom.CallLogManager.onCallStateChanged()"
 */

public class PrivateProvider extends ContentProvider{
    private static final String TAG = "PrivateProvider";
    //这里的AUTHORITY就是我们在AndroidManifest.xml中配置的authorities
    public static final String AUTHORITY = "com.transage.privatespace";
    //匹配成功后的匹配码
    public static final int MATCH_PHONE = 100;
    public static final int MATCH_SMS = 101;
    public static final int MATCH_CALL = 102;
    public static final int MATCH_APP = 103;
    public static UriMatcher uriMatcher;

    //在静态代码块中添加要匹配的 Uri
    static {
        //匹配不成功返回NO_MATCH(-1)
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        /**
         * uriMatcher.addURI(authority, path, code); 其中
         * authority：主机名(用于唯一标示一个ContentProvider,这个需要和清单文件中的authorities属性相同)
         * path:路径路径(可以用来表示我们要操作的数据，路径的构建应根据业务而定)
         * code:返回值(用于匹配uri的时候，作为匹配成功的返回值)
         */
        uriMatcher.addURI(AUTHORITY, "phone", MATCH_PHONE);// 匹配电话
        uriMatcher.addURI(AUTHORITY, "sms", MATCH_SMS);// 匹配短信
        uriMatcher.addURI(AUTHORITY, "call", MATCH_CALL);// 匹配通话记录
        uriMatcher.addURI(AUTHORITY, "app", MATCH_APP);// 匹配应用
    }

    DatabaseAdapter mDbAdapter;

    @Override
    public boolean onCreate() {
        if (mDbAdapter == null){
            mDbAdapter = new DatabaseAdapter(getContext());
            Log.i(TAG, "onCreate");
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query uri = " + uri.toString() + "selectionArgs" + selectionArgs[0]);
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case MATCH_PHONE:
                //查询手机号码
                cursor = mDbAdapter.getContactCursor(selection, selectionArgs, ImportExportUtils.isVcf(getContext()));
                break;
            case MATCH_SMS:
                //查询短信
                Log.e("wangmeng","=======MATCH_SMS query======>");
                cursor = mDbAdapter.getSmsCursor(selection,selectionArgs);
                break;
            case MATCH_CALL:
                //查询通话记录
                break;
            case MATCH_APP:
                //查询应用
                cursor = mDbAdapter.getAppCursor();
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.i(TAG, "uri = " +uri.toString()+ "insert contentValues = " + contentValues.toString());
        switch (uriMatcher.match(uri)) {
            case MATCH_PHONE:
                //insert手机号码
                break;
            case MATCH_SMS:
                //insert短信
                Log.e("wangmeng","=======MATCH_SMS insert======>");
                mDbAdapter.insertSms(contentValues);
                break;
            case MATCH_CALL:
                //insert通话记录
                mDbAdapter.insertCallLog(contentValues);
                break;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
