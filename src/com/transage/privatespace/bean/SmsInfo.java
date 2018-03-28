package com.transage.privatespace.bean;

/**
 * Created by dongrp on 2016/9/12.
 */
public class SmsInfo {
    //短信消息序号
    private int id;
    //对话序号
    private long thread_id;
    //电话号码
    private String address;
    //内容
    private String body;
    //日期
    private long date;
    //发件人
    private String person;
    //类型 发送或接收
    private int type;
    //是否已读
    private String read;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public long  getThread_id() {
        return thread_id;
    }

    public void setThread_id(long thread_id) {
        this.thread_id = thread_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }
    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
