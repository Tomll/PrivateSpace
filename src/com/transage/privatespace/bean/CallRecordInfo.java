package com.transage.privatespace.bean;

import android.graphics.Bitmap;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.utils.BitmapUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongrp on 2016/9/6.
 * 通话记录实体类
 */
public class CallRecordInfo implements Serializable {
    private byte[] photoData;
    private List<CallRecord> mCallRecords = new ArrayList<>();

    public List<CallRecord> getmCallRecords() {
        return mCallRecords;
    }

    public void addCallRecord(CallRecord record){
        mCallRecords.add(record);
    }

    public CallRecord getCallRecord(){
        if (mCallRecords.size()>0){
            return mCallRecords.get(counts()-1);
        }
        return null;
    }

    public List<CallRecord> getCallRecordList(){
        return mCallRecords;
    }

    public String name(){
        if (mCallRecords.size()>0){
            return mCallRecords.get(counts()-1).getName();
        }
        return "";
    }

    public long date(){
        if (mCallRecords.size()>0){
            return mCallRecords.get(counts()-1).getDate();
        }
        return 0;
    }

    public String phone(){
        if (mCallRecords.size()>0){
            return mCallRecords.get(counts()-1).getPhoneNum();
        }
        return "";
    }

    public int type(){
        int type = -1;
        if (mCallRecords.size() > 0){
            type = mCallRecords.get(counts()-1).getType();
        }
        return type;
    }

    public Bitmap photo(){
        return BitmapUtils.byteArray2Bitmap(photoData);
    }

    public int counts(){
        return mCallRecords.size();
    }

    public boolean exists(String phone){
        boolean result = false;
        if (mCallRecords != null && mCallRecords.size() > 0 && phone.equals(mCallRecords.get(0).getPhoneNum())){
            result = true;
        }
        return result;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }
}