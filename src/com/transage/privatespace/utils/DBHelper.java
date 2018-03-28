package com.transage.privatespace.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dongrp on 2016/8/24.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PrivateSpace.db"; //数据库名字
    private static final int DATABASE_VERSION = 1; //数据库版本
    //建表语句（表名apps）
    public static final String CREATE_APPS = "create table apps ("
            + "id integer primary key autoincrement,"
            + "packageName text, "
            + "appName text)";
    //建表语句（表名apps）
    public static final String CREATE_PEOPLES = "create table peoples ("
            + "id integer primary key autoincrement,"
            + "name text, "
            + "phoneNum text)";

    //构造
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        //String sql = "create table apps(packageName varchar(60) not null , appName varchar(60) not null );";
        db.execSQL(CREATE_APPS);
        db.execSQL(CREATE_PEOPLES);

    }

    //更新数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
