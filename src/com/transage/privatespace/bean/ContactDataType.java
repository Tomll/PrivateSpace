package com.transage.privatespace.bean;

/**
 * 联系人data数据类型
 * Created by yanjie.xu on 2017/9/22.
 */

public interface ContactDataType {
    public static final int NO_TYPE = -0x01;
    public static final int PHONE_TYPE = 0x01;
    public static final int EMAIL_TYPE = 0x02;
    public static final int EVENT_TYPE = 0x03;
    public static final int IM_TYPE = 0x04;
    public static final int ORGNIZATION_TYPE = 0x05;
    public static final int STRUCTUREDPOSTAL_TYPE = 0x06;
    public static final int OTHER_TYPE = 0x07;
}
