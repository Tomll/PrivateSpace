package com.transage.privatespace.bean;

import java.io.Serializable;

/**
 * Created by dongrp on 2016/9/2.
 * 联系人实体类
 */
public class People implements Serializable {
    private int id;
    private String displayName;
    private String phoneNum;
    private int rawContactId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(int rawContactId) {
        this.rawContactId = rawContactId;
    }

    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", rawContactId=" + rawContactId +
                '}';
    }

    public Object[] getColumnData(){
        return new Object[]{id, rawContactId, displayName, phoneNum};
    }
}
