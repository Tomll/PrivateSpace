package com.transage.privatespace.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Base64;
import android.util.Log;

import com.transage.privatespace.bean.ContactDataType;
import com.transage.privatespace.bean.ContactInfo;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 手机联系人操作帮助类
 * Created by yanjie.xu on 2017/8/24.
 */

public class ContactUtils {
    public static final String TAG = "ContactUtil";
    private static Context mContext;
    private List<ContactInfo> mContactInfos = new ArrayList<>();

    public ContactUtils(Context context) {
        this.mContext = context;
    }

    // ContactsContract.Contacts.CONTENT_URI= content://com.android.contacts/contacts;
    // ContactsContract.Data.CONTENT_URI = content://com.android.contacts/data;

    /**
     * 分页查询系统联系人信息
     *
     * @param pageSize      每页最大的数目
     * @param currentOffset 当前的偏移量
     * @return
     */
    public List<ContactInfo> getContactsByPage(int pageSize, int currentOffset) {
//        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key"};
//        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null,
//                "sort_key COLLATE LOCALIZED asc limit " + pageSize + " offset " + currentOffset);
        mContactInfos.clear();
        Cursor cur = mContext.getContentResolver().query(Contacts.CONTENT_URI, null, null, null,
                " sort_key desc limit " + pageSize + " offset " + currentOffset);
        if (cur.moveToFirst()) {
            do {
                ContactInfo info = new ContactInfo();
                // 获取联系人id号
                int id = cur.getInt(cur.getColumnIndex(Contacts._ID));
                // 获取联系人姓名
                String displayName = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
//                ContactInfo info = new ContactInfo(displayName);// 初始化联系人信息
                // 查看联系人有多少电话号码, 如果没有返回0
                int phoneCount = cur.getInt(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER));
                //得到联系人头像ID
                int photoid = cur.getInt(cur.getColumnIndex(Contacts.PHOTO_ID));
                //头像文件id
                int photoFileId = cur.getInt(cur.getColumnIndex(Contacts.PHOTO_FILE_ID));

                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if (photoid > 0) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
//                    contactPhoto = BitmapFactory.decodeStream(input);
                    info.setPhotoFileId(photoFileId);
                    info.setPhotoData(input2byte(input));
                } else {
//                    info.setPhoto(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_person_black_128dp));
                }
                info.setContactId(id);
                info.setDisplayName(displayName);
                info.setHasPhoneNumber(phoneCount);

                getRawContact(id, info);
                Log.i(TAG, "info = " + info.toString());
                mContactInfos.add(info);
            } while (cur.moveToNext());
            cur.close();
        }

        return mContactInfos;
    }

    /**
     * 获得系统联系人的所有记录数目
     *
     * @return
     */
    public int getAllCounts() {
        int num = 0;
        // 使用ContentResolver查找联系人数据
        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null,
                null, null);

        // 遍历查询结果，获取系统中所有联系人
        while (cursor.moveToNext()) {
            num++;
        }
        cursor.close();
        return num;
    }

    //获取手机中所有联系人数据
    public List<ContactInfo> getContacts(long contactId) {
        String selection = null;
        String[] selectionArgs = null;
        if (contactId > 0){
            selection = Contacts._ID + "=?";
            selectionArgs = new String[]{String.valueOf(contactId)};
        }
        Cursor cur = mContext.getContentResolver().query(Contacts.CONTENT_URI, null, selection, selectionArgs, null);
        if (cur.moveToFirst()) {
            do {
                ContactInfo info = new ContactInfo();
                // 获取联系人id号
                int id = cur.getInt(cur.getColumnIndex(Contacts._ID));
                // 获取联系人姓名
                String displayName = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
//                ContactInfo info = new ContactInfo(displayName);// 初始化联系人信息
                // 查看联系人有多少电话号码, 如果没有返回0
                int phoneCount = cur.getInt(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER));
                //得到联系人头像ID
                int photoid = cur.getInt(cur.getColumnIndex(Contacts.PHOTO_ID));
                //头像文件id
                int photoFileId = cur.getInt(cur.getColumnIndex(Contacts.PHOTO_FILE_ID));
                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if (photoid > 0) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
//                    contactPhoto = BitmapFactory.decodeStream(input);
                    info.setPhotoFileId(photoFileId);
                    info.setPhotoData(input2byte(input));
                } else {
//                    info.setPhoto(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_person_black_128dp));
                }
                info.setContactId(id);
                info.setDisplayName(displayName);
                info.setHasPhoneNumber(phoneCount);

                getRawContact(id, info);
                Log.i(TAG, "info = " + info.toString());
                mContactInfos.add(info);
            } while (cur.moveToNext());
            cur.close();
        }

        return mContactInfos;
    }

    public void getRawContact(int contactId, ContactInfo info) {
        //获取联系人RawContacts数据
        Cursor rawContactCursor = mContext.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.RawContacts.ACCOUNT_NAME},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)},
                null);
        Log.i(TAG, "rawContactCursor.getCount() = " + rawContactCursor.getCount() + "contactId = " + contactId);
        if (rawContactCursor.moveToFirst()) {
            do {
                // 遍历所有电话号码
                int _id = rawContactCursor.getInt(rawContactCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                int contact_id = rawContactCursor.getInt(rawContactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                String account_type = rawContactCursor.getString(rawContactCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                String account_name = rawContactCursor.getString(rawContactCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                ContactInfo.RawContactInfo rawContactInfo = new ContactInfo.RawContactInfo();
                rawContactInfo.contactId = contact_id;
                rawContactInfo.rawContactId = _id;
                rawContactInfo.accountType = account_type;
                rawContactInfo.accountName = account_name;
                getContactData(_id, rawContactInfo);
                info.addRawContact(rawContactInfo);
            } while (rawContactCursor.moveToNext());
            // 设置联系人电话信息
        }
    }

    /**
     * 获取联系人信息
     *
     * @return
     * @throws JSONException
     */
    public void getContactData(int rawContactId, ContactInfo.RawContactInfo info) {
        String mimetype = "";
        String type = "";
        int oldrid = -1;
        int contactId = -1;

        String project1[] = new String[]{
                "mimetype", "raw_contact_id", "data1", "data2", "data3", "data4", "data5", "data6", "data7",
                "data8", "data9", "data10", "data11", "data12", "data13", "data14", "data15"
        };

        // 1.查询通讯录所有联系人信息，通过id排序，我们看下android联系人的表就知道，所有的联系人的数据是由RAW_CONTACT_ID来索引开的
        // 所以，先获取所有的人的RAW_CONTACT_ID
        Cursor cursor = mContext.getContentResolver().query(
                Data.CONTENT_URI,
                project1,
                Data.RAW_CONTACT_ID + "=?",
                new String[]{String.valueOf(rawContactId)},
                Data.RAW_CONTACT_ID);
        int numm = 0;
        Log.i(TAG, "datainfo cursor getcount = " + cursor.getCount());
        while (cursor.moveToNext()) {
            contactId = cursor.getInt(cursor
                    .getColumnIndex(Data.RAW_CONTACT_ID));
//            if (oldrid != contactId) {
//                numm++;
//                oldrid = contactId;
//            }
            //Data数据类型
            int dataType = ContactDataType.NO_TYPE;

            ContactInfo.DataInfo dataInfo = new ContactInfo.DataInfo();
            mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE)); // 取得mimetype类型,扩展的数据都在这个类型里面
            type = cursor.getString(cursor.getColumnIndex(Data.DATA2));
            dataInfo.rawContactId = contactId;
            dataInfo.mimeType = mimetype;
            dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Data.DATA1));
            dataInfo.data2 = type;
            // 1.1,拿到联系人的各种名字
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data1 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.DISPLAY_NAME));
                dataInfo.data4 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.PREFIX));
                dataInfo.data3 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.FAMILY_NAME));
                dataInfo.data5 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.MIDDLE_NAME));
                dataInfo.data2 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.GIVEN_NAME));
                dataInfo.data6 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.SUFFIX));
                dataInfo.data9 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.PHONETIC_FAMILY_NAME));
                dataInfo.data8 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.PHONETIC_MIDDLE_NAME));
                dataInfo.data7 = cursor.getString(cursor
                        .getColumnIndex(StructuredName.PHONETIC_GIVEN_NAME));
                dataType = ContactDataType.NO_TYPE;
            }
            // 1.2 获取各种电话信息
            if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Phone.NUMBER)).
                        replaceAll(" ", "").replaceAll("-", "").trim();
                dataType = ContactDataType.PHONE_TYPE;
            }
            //查找Email信息
            if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
                dataType = ContactDataType.EMAIL_TYPE;
            }
            // 查找event地址
            if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出时间类型
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
                dataType = ContactDataType.EVENT_TYPE;
            }
            // 获取即时通讯消息
            if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出即时消息类型
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Im.DATA));
                dataType = ContactDataType.IM_TYPE;
            }
            // 获取备注信息
            if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Note.NOTE));
                dataType = ContactDataType.NO_TYPE;
            }
            // 获取昵称信息
            if (Nickname.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Nickname.NAME));
                dataType = ContactDataType.NO_TYPE;
            }
            // 获取组织信息
            if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出组织类型
                dataInfo.data1 = cursor.getString(cursor
                        .getColumnIndex(Organization.COMPANY));
                dataInfo.data4 = cursor.getString(cursor
                        .getColumnIndex(Organization.TITLE));
                dataInfo.data5 = cursor.getString(cursor
                        .getColumnIndex(Organization.DEPARTMENT));
                dataType = ContactDataType.ORGNIZATION_TYPE;
            }
            // 获取网站信息
            if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出组织类型
                dataInfo.data1 = cursor.getString(cursor.getColumnIndex(Website.URL));
                dataType = ContactDataType.NO_TYPE;
            }
            // 查找通讯地址
            if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出邮件类型
                dataInfo.data4 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.STREET));
                dataInfo.data7 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.CITY));
                dataInfo.data5 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.POBOX));
                dataInfo.data6 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.NEIGHBORHOOD));
                dataInfo.data8 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.REGION));
                dataInfo.data9 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.POSTCODE));
                dataInfo.data10 = cursor.getString(cursor
                        .getColumnIndex(StructuredPostal.COUNTRY));
                dataType = ContactDataType.STRUCTUREDPOSTAL_TYPE;
            }
            //查找头像数据
            if (Photo.CONTENT_ITEM_TYPE.equals(mimetype)) {
                dataInfo.data14 = cursor.getString(cursor.getColumnIndex(Photo.PHOTO_FILE_ID));
                byte[] buff = cursor.getBlob(cursor.getColumnIndex(Photo.PHOTO));
                if (buff != null) {
                    dataInfo.data15 = Base64.encodeToString(buff, Base64.DEFAULT);
                    Log.d(TAG, "get photo data15 buff.lenth = " + buff.length);
                }
                dataType = ContactDataType.NO_TYPE;
            }

            try {
                //type可已转化为int时，通过getLabelNameByType获取typename
                dataInfo.typeName = getLabelNameByType(Integer.parseInt(type), dataType);
            } catch (NumberFormatException e) {
                //type无法转化为int时，直接赋值给typename
                dataInfo.typeName = type;
            }
//            Log.i(TAG, "dataInfo = " + dataInfo.toString());
            info.dataInfos.add(dataInfo);
        }
        cursor.close();
//        Log.i(TAG, contactData.toString());
    }

    /**
     * inputStream转换为byte数组
     *
     * @param inStream
     * @return
     */
    public static final byte[] input2byte(InputStream inStream) {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[512];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 512)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] in2b = swapStream.toByteArray();
        Log.d(TAG, "[input2byte] photo.lenth = " + in2b.length);
        return in2b;
    }

    /**
     * 根据type值获取标签字符串
     *
     * @param type
     * @return
     */
    public static String getLabelNameByType(int type, int dataType) {
        if (dataType == ContactDataType.NO_TYPE){
            return "";
        }
        int typeLabelResource = 0;
        switch (dataType){
            case ContactDataType.PHONE_TYPE:
                //通过type获取typelabel字符串
                typeLabelResource= Phone.getTypeLabelResource(type);
                break;
            case ContactDataType.EMAIL_TYPE:
                //通过type获取typelabel字符串
                typeLabelResource= Email.getTypeLabelResource(type);
                break;
            case ContactDataType.EVENT_TYPE:
                typeLabelResource= Event.getTypeResource(type);
                break;
            case ContactDataType.IM_TYPE:
                typeLabelResource= Im.getTypeLabelResource(type);
                break;
            case ContactDataType.ORGNIZATION_TYPE:
                typeLabelResource= Organization.getTypeLabelResource(type);
                break;
            case ContactDataType.STRUCTUREDPOSTAL_TYPE:
                typeLabelResource= StructuredPostal.getTypeLabelResource(type);
                break;
        }
        if (typeLabelResource == 0){
            return "";
        }
        return mContext.getResources().getString(typeLabelResource);
//        Log.i(TAG, "[getLabelNameByType] type = " + type + " string = " + string);
    }
}
