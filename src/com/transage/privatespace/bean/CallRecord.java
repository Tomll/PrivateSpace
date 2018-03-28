package com.transage.privatespace.bean;

import java.io.Serializable;

/**
 * Created by yanjie.xu on 2017/8/2.
 *
 * 通话记录对象类
 */
public class CallRecord implements Serializable {
    private int id;
    private long date;
    private String phoneNum;
    private String name;
    private int type;
    private int duration;
    private int photoFileId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPhotoFileId() {
        return photoFileId;
    }

    public void setPhotoFileId(int photoFileId) {
        this.photoFileId = photoFileId;
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                "id=" + id +
                ", date=" + date +
                ", phoneNum='" + phoneNum + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                ", photoFileId=" + photoFileId +
                '}';
    }
}