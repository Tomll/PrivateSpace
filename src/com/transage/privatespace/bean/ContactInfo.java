package com.transage.privatespace.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;

import com.transage.privatespace.utils.BitmapUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 联系人信息包装类
 * <p>
 * Created by yanjie.xu on 2017/8/24.
 */
public class ContactInfo implements Serializable {
    private static final String TAG = "ContactInfo";
    private int contactId;
    private int hasPhoneNumber;
    private int photoId;
    private int photoFileId;
    private String displayName;
    private byte[] photoData;
    private List<RawContactInfo> rawContactInfos = new ArrayList<>();
    private List<Phone> phones = new ArrayList<>();

    public static class RawContactInfo implements Serializable{
        public int rawContactId;
        public int contactId;
        public String accountName;
        public String accountType;
        public List<DataInfo> dataInfos = new ArrayList<>();

        @Override
        public String toString() {
            return "RawContactInfo{" +
                    "rawContactId=" + rawContactId +
                    ", contactId=" + contactId +
                    ", accountName='" + accountName + '\'' +
                    ", accountType='" + accountType + '\'' +
                    ", dataInfos=" + dataInfos +
                    '}';
        }
    }

    public static class DataInfo implements Serializable{
        public int id;
        public String mimeType;
        public String typeName;
        public int rawContactId;
        public String data1;
        public String data2;
        public String data3;
        public String data4;
        public String data5;
        public String data6;
        public String data7;
        public String data8;
        public String data9;
        public String data10;
        public String data11;
        public String data12;
        public String data13;
        public String data14;
        public String data15;

        @Override
        public String toString() {
            return "DataInfo{" +
                    "mimeType='" + mimeType + '\'' +
                    ", typeName='" + typeName + '\'' +
                    ", rawContactId=" + rawContactId +
                    ", data1='" + data1 + '\'' +
                    ", data2='" + data2 + '\'' +
                    ", data3='" + data3 + '\'' +
                    ", data4='" + data4 + '\'' +
                    ", data5='" + data5 + '\'' +
                    ", data6='" + data6 + '\'' +
                    ", data7='" + data7 + '\'' +
                    ", data8='" + data8 + '\'' +
                    ", data9='" + data9 + '\'' +
                    ", data10='" + data10 + '\'' +
                    ", data11='" + data11 + '\'' +
                    ", data12='" + data12 + '\'' +
                    ", data13='" + data13 + '\'' +
                    ", data14='" + data14 + '\'' +
                    ", data15='" + data15 + '\'' +
                    '}';
        }
    }

    public static class Phone implements Serializable{
        public String phoneNumber;
        public String typeName;

        @Override
        public String toString() {
            return "Phone{" +
                    "phoneNumber='" + phoneNumber + '\'' +
                    ", typeName='" + typeName + '\'' +
                    '}';
        }
    }

    public List<RawContactInfo> getRawContactInfos() {
        return rawContactInfos;
    }

    public void setRawContactInfos(List<RawContactInfo> rawContactInfos) {
        this.rawContactInfos = rawContactInfos;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getHasPhoneNumber() {
        return hasPhoneNumber;
    }

    public void setHasPhoneNumber(int hasPhoneNumber) {
        this.hasPhoneNumber = hasPhoneNumber;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getPhotoFileId() {
        return photoFileId;
    }

    public void setPhotoFileId(int photoFileId) {
        this.photoFileId = photoFileId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    /**
     * 获取该联系人中的所有电话号码
     * @return
     */
    public List<Phone> getPhones() {
        phones.clear();
        for (RawContactInfo rawContactInfo : rawContactInfos) {
            for (DataInfo dataInfo : rawContactInfo.dataInfos) {
                if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(dataInfo.mimeType)) {
                    Phone phone = new Phone();
                    phone.phoneNumber = dataInfo.data1;
                    phone.typeName = dataInfo.typeName;
                    Log.i(TAG, "[getPhones] phone = " + phone.toString());
                    phones.add(phone);
                }
            }
        }
        return phones;
    }

    /**
     * 判断该联系人能否加入私密
     * @return
     */
    public boolean isPrivateEnable() {
        boolean enable = true;
        for (RawContactInfo rawContactInfo : rawContactInfos) {
            //如果联系人不是sim卡联系人则允许加入私密
            if (rawContactInfo.accountName!=null && rawContactInfo.accountName.contains("SIM")){
                enable = false;
            }
        }
        return enable;
    }

    /**
     * 获取所有rawContactId
     * @return
     */
    public int[] getRawContactIds() {
        int[] rci = new int[10];
        int i = -1;
        for (RawContactInfo rawContactInfo : rawContactInfos) {
            if (i < 10){
                i++;
                rci[i] = rawContactInfo.rawContactId;
            }
        }
        return rci;
    }

    public void addRawContact(RawContactInfo info) {
        rawContactInfos.add(info);
    }

    /**
     * 获取联系人头像图片
     * @return
     */
    public Bitmap getPhoto() {
        return BitmapUtils.byteArray2Bitmap(photoData);
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "contactId=" + contactId +
                ", hasPhoneNumber=" + hasPhoneNumber +
                ", photoFileId=" + photoFileId +
                ", photoData=" + photoData +
                ", displayName='" + displayName + '\'' +
                ", rawContactInfos=" + rawContactInfos +
                ", phones=" + phones +
                '}';
    }
}
