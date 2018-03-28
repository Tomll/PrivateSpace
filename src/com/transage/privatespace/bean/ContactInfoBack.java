package com.transage.privatespace.bean;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Data;
import android.util.Log;
import android.widget.Toast;

import com.transage.privatespace.R;
import com.transage.privatespace.vcard.pim.VDataBuilder;
import com.transage.privatespace.vcard.pim.VNode;
import com.transage.privatespace.vcard.pim.vcard.ContactStruct;
import com.transage.privatespace.vcard.pim.vcard.ContactStruct.ContactMethod;
import com.transage.privatespace.vcard.pim.vcard.ContactStruct.PhoneData;
import com.transage.privatespace.vcard.pim.vcard.VCardComposer;
import com.transage.privatespace.vcard.pim.vcard.VCardException;
import com.transage.privatespace.vcard.pim.vcard.VCardParser;
import com.transage.privatespace.vcard.provider.Contacts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 联系人信息包装类
 * <p>
 * Created by yanjie.xu on 2017/7/19.
 */
public class ContactInfoBack {
    private static final String TAG = "ContactInfoBack";
    /**
     * MUST exist
     */
    private String name; // 姓名

    /**
     * 联系人电话信息
     */
    public static class PhoneInfo {
        /**
         * 联系电话类型
         */
        public int type;
        /**
         * 联系电话
         */
        public String number;
    }

    /**
     * 联系人邮箱信息
     */
    public static class EmailInfo {
        /**
         * 邮箱类型
         */
        public int type;
        /**
         * 邮箱
         */
        public String email;
    }

    private List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>(); // 联系号码

    private List<EmailInfo> email = new ArrayList<EmailInfo>(); // Email

    /**
     * 构造联系人信息
     *
     * @param name 联系人姓名
     */
    public ContactInfoBack(String name) {
        this.name = name;
    }

    /**
     * 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 姓名
     */
    public ContactInfoBack setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 联系电话信息
     */
    public List<PhoneInfo> getPhoneList() {
        return phoneList;
    }

    /**
     * 联系电话信息
     */
    public ContactInfoBack setPhoneList(List<PhoneInfo> phoneList) {
        this.phoneList = phoneList;
        return this;
    }

    /**
     * 邮箱信息
     */
    public List<EmailInfo> getEmail() {
        return email;
    }

    /**
     * 邮箱信息
     */
    public ContactInfoBack setEmail(List<EmailInfo> email) {
        this.email = email;
        return this;
    }

    @Override
    public String toString() {
        return "{name: " + name + ", number: " + phoneList + ", email: " + email + "}";
    }


    /**
     * 联系人
     * 备份/还原操作
     *
     * @author LW
     */
    public static class ContactHandler {
        private static ContactHandler instance_ = new ContactHandler();

        /**
         * 获取实例
         */
        public static ContactHandler getInstance() {
            return instance_;
        }

        /**
         * 获取联系人指定信息
         *
         * @param projection 指定要获取的列数组, 获取全部列则设置为null
         * @return
         * @throws Exception
         */

        public Cursor queryContact(Activity context, String[] projection) {
            // 获取联系人的所需信息
            Cursor cur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
            return cur;
        }


        /**
         * 获取联系人信息
         *
         * @param context
         * @return
         */
        public List<ContactInfoBack> getContactInfoBack(Activity context) {
            List<ContactInfoBack> infoList = new ArrayList<ContactInfoBack>();
            Cursor cur = queryContact(context, null);
            if (cur.moveToFirst()) {
                do {
                    // 获取联系人id号
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    // 获取联系人姓名
                    String displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    ContactInfoBack info = new ContactInfoBack(displayName);// 初始化联系人信息
                    // 查看联系人有多少电话号码, 如果没有返回0
                    int phoneCount = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

//                    ContentValues values = new ContentValues();
//                    values.put(ContactsContract.Contacts.IS_PRIVATE_CONTACTS, 0);
//                    context.getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.NAME_RAW_CONTACT_ID + "= ?", null);
                    Log.i(TAG, "getContactInfoBack()->info = " + info.toString());

                    if (phoneCount > 0) {
                        Cursor phonesCursor = context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[]{id}, null);
                        Log.i(TAG, "getContactInfoBack()->info = " + info.toString());
                        if (phonesCursor.moveToFirst()) {
                            List<PhoneInfo> phoneNumberList = new ArrayList<PhoneInfo>();
                            do {
                                // 遍历所有电话号码
                                String phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                                // 对应的联系人类型
                                int type = phonesCursor.getInt(phonesCursor.getColumnIndex(Phone.TYPE));
                                // 初始化联系人电话信息
                                PhoneInfo phoneInfo = new PhoneInfo();
                                phoneInfo.type = type;
                                phoneInfo.number = phoneNumber;
                                phoneNumberList.add(phoneInfo);
                            } while (phonesCursor.moveToNext());
                            // 设置联系人电话信息
                            info.setPhoneList(phoneNumberList);
                        }
                    }

                    // 获得联系人的EMAIL
                    Cursor emailCur = context.getContentResolver().query(Email.CONTENT_URI,
                            null, Email.CONTACT_ID + "=" + id, null, null);
                    if (emailCur.moveToFirst()) {
                        List<EmailInfo> emailList = new ArrayList<EmailInfo>();
                        do {
                            // 遍历所有的email
                            String email = emailCur.getString(emailCur.getColumnIndex(Email.DATA1));
                            int type = emailCur.getInt(emailCur.getColumnIndex(Email.TYPE));
                            // 初始化联系人邮箱信息
                            EmailInfo emailInfo = new EmailInfo();
                            emailInfo.type = type;    // 设置邮箱类型
                            emailInfo.email = email;    // 设置邮箱地址
                            emailList.add(emailInfo);
                        } while (emailCur.moveToNext());
                        info.setEmail(emailList);
                    }

                    //Cursor postalCursor = getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + "=" + id, null, null);
                    Log.i(TAG, "getContactInfoBack()->info = " + info.toString());
                    infoList.add(info);

                } while (cur.moveToNext());
            }
            return infoList;
        }

        /**
         * 备份联系人
         */
        public void backupContacts(Activity context, List<ContactInfoBack> infos) {

            try {
                String path = Environment.getExternalStorageDirectory() + "/contacts.vcf";
                Log.i(TAG, "backupContacts()->file = " + path);
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
                VCardComposer composer = new VCardComposer();
                for (ContactInfoBack info : infos) {
                    ContactStruct contact = new ContactStruct();
                    contact.name = info.getName();
                    // 获取联系人电话信息, 添加至 ContactStruct
                    List<PhoneInfo> numberList = info
                            .getPhoneList();
                    for (PhoneInfo phoneInfo : numberList) {
                        contact.addPhone(phoneInfo.type, phoneInfo.number, null, true);
                    }
                    // 获取联系人Email信息, 添加至 ContactStruct
                    List<EmailInfo> emailList = info.getEmail();
                    for (EmailInfo emailInfo : emailList) {
                        contact.addContactmethod(Contacts.KIND_EMAIL, emailInfo.type, emailInfo.email, null, true);
                    }
                    String vcardString = composer.createVCard(contact, VCardComposer.VERSION_VCARD30_INT);
                    writer.write(vcardString);
                    writer.write("\n");
                    writer.flush();
                }
                writer.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (VCardException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(context, context.getString(R.string.backup_successful), Toast.LENGTH_SHORT).show();
        }

        /**
         * 获取vCard文件中的联系人信息
         *
         * @return
         */
        public List<ContactInfoBack> restoreContacts() throws Exception {
            List<ContactInfoBack> ContactInfoBackList = new ArrayList<ContactInfoBack>();

            VCardParser parse = new VCardParser();
            VDataBuilder builder = new VDataBuilder();
            String file = Environment.getExternalStorageDirectory() + "/contacts.vcf";
            Log.i(TAG, "restoreContacts()->file = " + file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String vcardString = "";
            String line;
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            Log.i(TAG, "restoreContacts()->vcardString = " + vcardString);
            boolean parsed = parse.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + file);
            }
            List<VNode> pimContacts = builder.vNodeList;
            for (VNode contact : pimContacts) {
                ContactStruct contactStruct = ContactStruct.constructContactFromVNode(contact, 1);
                // 获取备份文件中的联系人电话信息
                List<PhoneData> phoneDataList = contactStruct.phoneList;
                List<PhoneInfo> phoneInfoList = new ArrayList<PhoneInfo>();
                for (PhoneData phoneData : phoneDataList) {
                    PhoneInfo phoneInfo = new PhoneInfo();
                    phoneInfo.number = phoneData.data;
                    phoneInfo.type = phoneData.type;
                    phoneInfoList.add(phoneInfo);
                }
                // 获取备份文件中的联系人邮箱信息
                List<ContactMethod> emailList = contactStruct.contactmethodList;
                List<EmailInfo> emailInfoList = new ArrayList<EmailInfo>();
                // 存在 Email 信息
                if (null != emailList) {
                    for (ContactMethod contactMethod : emailList) {
                        if (Contacts.KIND_EMAIL == contactMethod.kind) {
                            EmailInfo emailInfo = new EmailInfo();
                            emailInfo.email = contactMethod.data;
                            emailInfo.type = contactMethod.type;
                            emailInfoList.add(emailInfo);
                        }
                    }
                }
                ContactInfoBack info = new ContactInfoBack(contactStruct.name).setPhoneList(phoneInfoList).setEmail(emailInfoList);
                ContactInfoBackList.add(info);
            }
            return ContactInfoBackList;
        }

        /**
         * 向手机中录入联系人信息
         *
         * @param info 要录入的联系人信息
         */
        public void addContacts(Activity context, ContactInfoBack info) {
            ContentValues values = new ContentValues();
            //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);
            //往data表入姓名数据
            values.clear();
            values.put(Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            values.put(StructuredName.GIVEN_NAME, info.getName());
            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            // 获取联系人电话信息
            List<PhoneInfo> phoneList = info.getPhoneList();
            /** 录入联系电话 */
            for (PhoneInfo phoneInfo : phoneList) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                // 设置录入联系人电话信息
                values.put(Phone.NUMBER, phoneInfo.number);
                values.put(Phone.TYPE, phoneInfo.type);
                // 往data表入电话数据
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            }
            // 获取联系人邮箱信息
            List<EmailInfo> emailList = info.getEmail();
            /** 录入联系人邮箱信息 */
            for (EmailInfo email : emailList) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                // 设置录入的邮箱信息
                values.put(Email.DATA, email.email);
                values.put(Email.TYPE, email.type);
                // 往data表入Email数据
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            }
        }
    }
}

