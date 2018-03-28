package com.transage.privatespace.loader;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.internal.telephony.SmsApplication;
import com.transage.privatespace.R;
import com.transage.privatespace.activity.PrivateContacts;
import com.transage.privatespace.bean.CallRecord;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.bean.SmsInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.utils.NotificationUtils;
import com.transage.privatespace.utils.PhoneNumUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yanjie.xu on 2017/7/20.
 */

public class PrivateSpaceLoader {
    private static final String TAG = "PrivateSpaceLoader";

    private static final String VCF_FILE_PATH = "/test.vcf";
    public static final int REFRESHE_CALLRECORD = 1101;
    public static final int REFRESHE_CONTACT = 1102;
    public static final int REFRESHE_SMS = 1103;
    public static final int PRIVATE_OBSERVER = 1104;
    //数据加载监听器
    private static PrivateContacts mLoadListener;
    private Context mContext;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            OnLoadListener.LoadTag tag = OnLoadListener.LoadTag.CONTACT;
            switch (msg.what) {
                case REFRESHE_CALLRECORD:
                case PRIVATE_OBSERVER:
                    tag = OnLoadListener.LoadTag.CALLRECORD;
                    break;
                case REFRESHE_CONTACT:
                    tag = OnLoadListener.LoadTag.CONTACT;
                    break;
                case REFRESHE_SMS:
                    mLoadListener.setRestoreDefaultSms(mContext);
                    tag = OnLoadListener.LoadTag.SMS;
                    break;
                default:
                    break;
            }
            int action = msg.getData().getInt("action");
            int index = msg.getData().getInt("index");
            mLoadListener.onLoadComplete(tag, action, index);
        }
    };

    public PrivateSpaceLoader(PrivateContacts mLoadListener, Context context) {
        this.mLoadListener = mLoadListener;
        mContext = context;
    }

    /**
     * 设置私密联系人
     *
     * @param list_selectedContact ：待加密/解密的联系人列表
     * @param isPrivate            ：1表示加为私密 ，0表示解除私密
     * @param activity             ：内容解析者
     */
    public void loadPrivateContacts(List<ContactInfo> list_selectedContact, int isPrivate, Activity activity) {
        Log.i(TAG, "loadPrivateContacts()");
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(activity);
        mContext = activity.getApplicationContext();
        setDefaultSms(activity.getApplicationContext());//设置私密为默认短信

        if (isPrivate == 0) {//解除私密
            getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    int index = 0;
                    for (ContactInfo contactInfo : list_selectedContact) {
                        index++;

                        //还原联系人到系统中
                        addContacts2Db(activity, contactInfo);
                        //删除私密联系人
//                        databaseAdapter.deleteContactsByPhone(contactInfo.getPhoneNum(), ImportExportUtils.isVcf(activity));
                        databaseAdapter.deleteContactById(contactInfo.getContactId());

                        List<ContactInfo.Phone> phones = new CopyOnWriteArrayList<>();
                        phones.addAll(contactInfo.getPhones());
                        for (ContactInfo.Phone phone : phones) {
                            //批量插入通话记录
//                        batchAddCallLogs(activity, databaseAdapter.getCallRecordsByNum(people.getPhoneNum()));
                            addCallLogs(activity, databaseAdapter.getCallRecordsByNum(phone.phoneNumber));
                            //删除通话记录
                            databaseAdapter.deleteCallRecordByNum(phone.phoneNumber);
                            mHandler.sendEmptyMessage(REFRESHE_CALLRECORD);

                            String phoneNum = PhoneNumUtil.formatNumber(phone.phoneNumber);//格式化电话号码
                            //还原短信到数据库中
                            insertSmstoSystem(activity, databaseAdapter.getSmsByNum(phoneNum));
                            //删除私密短信
                            new DatabaseAdapter(activity).deleteSmsByAddress(phoneNum);
                            mHandler.sendEmptyMessage(REFRESHE_SMS);
                        }

//                        //当检测到该联系人没有电话号码时不继续向下走
//                        String phoneNumber = null;
//                        if (contactInfo.getPhones().size() > 0) {
//                            phoneNumber = contactInfo.getPhones().get(0).phoneNumber;
//                        }
//                        if (phoneNumber == null) {
//                            continue;
//                        }
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt("action", NotificationUtils.REMOVE_CONTACT_NOTITY_ID);
                        bundle.putInt("index", index);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            });
        } else if (isPrivate == 1) {//添加私密
            getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    //添加联系人到私密
//                    databaseAdapter.addContacts(list_selectedPeople, ImportExportUtils.isVcf(activity));
                    databaseAdapter.addContact(list_selectedContact);
                    int index = 0;
                    for (ContactInfo info : list_selectedContact) {
                        index++;
                        int[] rawContactIds = info.getRawContactIds();
                        for (int i = 0; i < rawContactIds.length; i++) {
                            //删除联系人元数据
                            activity.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                                    ContactsContract.Data._ID + "=?",
                                    new String[]{String.valueOf(rawContactIds[i])});
                        }
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt("action", NotificationUtils.ADD_CONTACT_NOTITY_ID);
                        bundle.putInt("index", index);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            });

            for (ContactInfo info : list_selectedContact) {
                getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        addCallLogToPrivate(info, activity);
                        mHandler.sendEmptyMessage(REFRESHE_CALLRECORD);
                    }
                });

                getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        //添加短信到私密 add by wangmeng 20170707
                        addSmstoPrivate(activity, info);
                        mHandler.sendEmptyMessage(REFRESHE_SMS);
                    }
                });
            }
        }
    }

    //wangmeng 20170802 setting default mms start
    public void setDefaultSms(Context context) {
        Log.e("wangmeng", "###setDefaultSms###### " + context.getPackageName());
        SmsApplication.setDefaultApplication(context.getPackageName(), context);
    }

    //wangmeng 20170802 setting default mms end
    public void addCallLogToPrivate(ContactInfo contactInfo, Activity activity) {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(activity);
        List<CallRecord> callRecords = getCallRecordsFromContectsProvider(contactInfo, activity);
        if (callRecords != null && !callRecords.isEmpty()) {
            for (CallRecord callRecord : callRecords) {
                Log.i(TAG, "add callLog" + callRecord.toString());
                //添加通话记录到私密
                databaseAdapter.addCallRecord(callRecord);
                //删除通话记录到私密
                activity.getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?",
                        new String[]{callRecord.getPhoneNum().replaceAll(" ", "").replaceAll("-", "").trim()});
            }
        }

    }

    private List<CallRecord> getCallRecordsFromContectsProvider(ContactInfo contactInfo, Activity context) {
        // 检查并申请 READ_CALL_LOG 权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_CALL_LOG}, 1);
            return null;
        }
        List<CallRecord> callRecordsList = new ArrayList<CallRecord>();
        List<ContactInfo.Phone> phones = new CopyOnWriteArrayList<>();
        phones.addAll(contactInfo.getPhones());
        Log.i(TAG, "[getCallRecordsFromContectsProvider] contactInfo.getPhones().size = " + phones.size());
        for (ContactInfo.Phone phone : phones) {
            if (phone != null) {
                /**
                 * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）努力了
                 * @param projection 需要查询的字段
                 * @param selection sql语句where之后的语句
                 * @param selectionArgs ?占位符代表的数据
                 * @param sortOrder 排序方式
                 */
                Cursor recordCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
                        new String[]{CallLog.Calls._ID,
                                CallLog.Calls.CACHED_NAME,// 通话记录的联系人
                                CallLog.Calls.NUMBER,// 通话记录的电话号码
                                CallLog.Calls.DATE,// 通话记录的日期
                                CallLog.Calls.DURATION,// 通话时长
                                CallLog.Calls.TYPE}// 通话类型
                        , CallLog.Calls.NUMBER + "= ?",
                        new String[]{phone.phoneNumber.replaceAll(" ", "").replaceAll("-", "").trim()},
                        CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
                );
                if (recordCursor != null && recordCursor.moveToFirst()) {
                    do {
                        int callLogId = recordCursor.getInt(0);
                        String name = recordCursor.getString(1);
                        String phoneNum = recordCursor.getString(2);
                        long date = recordCursor.getLong(3);
                        int duration = recordCursor.getInt(4);
                        int type = recordCursor.getInt(5);
//                    if (TextUtils.isEmpty(name)){
                        name = contactInfo.getDisplayName();
//                    }
                        // 添加数据到list
                        CallRecord callRecord = new CallRecord();
                        callRecord.setDate(date);
                        callRecord.setPhoneNum(phoneNum.replaceAll(" ", "").replaceAll("-", "").trim());
                        callRecord.setName(name);
                        callRecord.setType(type);
                        callRecord.setPhotoFileId(contactInfo.getPhotoFileId());
                        callRecord.setDuration(duration);
                        callRecordsList.add(callRecord);
                    } while (recordCursor.moveToNext());
                    recordCursor.close();
                }
            }
        }
        Log.i(TAG, contactInfo.toString());

        return callRecordsList;
    }

    //还原短信数据到系统中
    public void insertSmstoSystem(Context context, List<SmsInfo> smsList) {
        ContentValues values = new ContentValues();
        for (SmsInfo sms : smsList) {
            values.clear();
            values.put("address", sms.getAddress());
            values.put("body", sms.getBody());
            values.put("date", String.valueOf(sms.getDate()));
            values.put("type", String.valueOf(sms.getType()));
            values.put("read", sms.getRead());
            context.getContentResolver().insert(Uri.parse("content://sms/"), values);
        }
    }

    //添加短信到私密 add by wangmeng 20170707
    public void addSmstoPrivate(Activity activity, ContactInfo contactInfo) {
        // 1.先获取到所有私密联系人
//        ArrayList<People> listPrivatePeople = new ArrayList<People>();
//        listPrivatePeople.addAll(new DatabaseAdapter(activity).getContacts());
        //Log.e("wangmeng","=====111111111========>");
        // 2.获取私密联系人的短信记录
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        String[] projection = new String[]{"_id", "thread_id", "address", "person", "body", "date", "type", "read"};
//        for (int i = 0; i < listPrivatePeople.size(); i++) {
//            String phone = listPrivatePeople.get(i).getPhoneNum();
        if (contactInfo != null) {
            Log.e("wangmeng", "=====222222222========>");
//            Cursor cursor2 = activity.getContentResolver().query(Uri.parse("content://sms/"), projection,
//                    "address=?", new String[]{phone.getPhoneNum()}, "date desc");
            List<ContactInfo.Phone> phones = new CopyOnWriteArrayList<>();
            phones.addAll(contactInfo.getPhones());
            for (ContactInfo.Phone phone : phones) {
                String phoneNum = phone.phoneNumber;
                Cursor cursor2 = activity.getContentResolver().query(Uri.parse("content://sms/"), projection, "address" + "  LIKE ? ",
                        new String[]{"%" + phoneNum + "%"}, "date asc");
                Log.e("wangmeng", "=====phone.getPhoneNum========>" + phoneNum);
                Log.e("wangmeng", "=====cursor2========>" + cursor2);
                Log.e("wangmeng", "=====cursor2.moveToFirst========>" + cursor2.moveToFirst());

                if (cursor2 != null && cursor2.moveToFirst()) {
                    // ArrayList<Sms> listSms = new ArrayList<Sms>();
                    do {
                        Log.e("wangmeng", "=====333333========>");
                        int id = cursor2.getInt(cursor2.getColumnIndex("_id"));
                        long threadId = cursor2.getLong(cursor2.getColumnIndex("thread_id"));
                        String address = cursor2.getString(cursor2.getColumnIndex("address"));// 手机号码
                        String body = cursor2.getString(cursor2.getColumnIndex("body"));// 短信内容
                        long date = cursor2.getLong(cursor2.getColumnIndex("date"));//收发时间
                        //String date = simpleDateFormat.format(new Date(cursor2.getLong(cursor2.getColumnIndex("date"))));// 收发时间
                        int type = cursor2.getInt(cursor2.getColumnIndex("type"));// 收发类型
                        String read = cursor2.getString(cursor2.getColumnIndex("read"));//是否阅读
//                        Log.e("wangmeng","======threadId: "+threadId);
//                        Log.e("wangmeng","======address: "+address);
//                        Log.e("wangmeng","======body: "+body);
                        Log.e("wangmeng", "======date: " + date);
                        Log.e("wangmeng", "======read: " + read);
//                        Log.e("wangmeng","======type: "+type);
                        // 添加一条短息数据到listSms
                        SmsInfo sms = new SmsInfo();
                        sms.setId(id);
                        sms.setThread_id(threadId);
                        sms.setAddress(PhoneNumUtil.formatNumber(address));
                        sms.setPerson(contactInfo.getDisplayName());
                        sms.setBody(body);
                        sms.setDate(date);
                        sms.setType(type);
                        sms.setRead(read);
                        new DatabaseAdapter(activity).addSms(sms);
                        //wangmeng delete mms data
                        int result = activity.getContentResolver().delete(Uri.parse("content://sms/"), "address = ?", new String[]{address});
                        Log.e("wangmeng", "=======result======>" + result);
                    } while (cursor2.moveToNext());
                    cursor2.close();
                }
            }
        }
    }

    /**
     * 添加联系人
     * 在同一个事务中完成联系人各项数据的添加
     * 使用ArrayList<ContentProviderOperation>，把每步操作放在它的对象中执行
     */
    private void addContacts2Db(Context context, ContactInfo contactInfo) {
        List<ContactInfo.RawContactInfo> rawContactInfos = contactInfo.getRawContactInfos();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        // 第一个参数：内容提供者的主机名
        // 第二个参数：要执行的操作
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        for (ContactInfo.RawContactInfo rawInfo : rawContactInfos) {
            // 操作1.添加Google账号，这里值为null，表示不添加
            ContentValues values = new ContentValues();
            values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, rawInfo.accountType);
            values.put(ContactsContract.RawContacts.ACCOUNT_NAME, rawInfo.accountName);
            Log.i(TAG, "[addContacts2Db] insert rawInfo.accountName begin = " + rawInfo.accountName);
            long contactId = 0;
            Uri uriInsert = null;
            try {
                uriInsert = resolver.insert(uri, values);
                Log.i(TAG, "[addContacts2Db] insert uriInsert = " + uriInsert);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if (uriInsert != null) {
                contactId = ContentUris.parseId(uriInsert);
            }

            Log.i(TAG, "[addContacts2Db] insert rawInfo.accountName end = " + rawInfo.accountName);

            for (ContactInfo.DataInfo data : rawInfo.dataInfos) {
                ContentValues values1 = new ContentValues();
                uri = Uri.parse("content://com.android.contacts/data");
                values1.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                values1.put(ContactsContract.Data.MIMETYPE, data.mimeType);
                values1.put(ContactsContract.Data.DATA1, data.data1);
                values1.put(ContactsContract.Data.DATA2, data.data2);
                values1.put(ContactsContract.Data.DATA3, data.data3);
                values1.put(ContactsContract.Data.DATA4, data.data4);
                values1.put(ContactsContract.Data.DATA5, data.data5);
                values1.put(ContactsContract.Data.DATA6, data.data6);
                values1.put(ContactsContract.Data.DATA7, data.data7);
                values1.put(ContactsContract.Data.DATA8, data.data8);
                values1.put(ContactsContract.Data.DATA9, data.data9);
                values1.put(ContactsContract.Data.DATA10, data.data10);
                values1.put(ContactsContract.Data.DATA11, data.data11);
                values1.put(ContactsContract.Data.DATA12, data.data12);
                values1.put(ContactsContract.Data.DATA13, data.data13);
                values1.put(ContactsContract.Data.DATA14, data.data14);
                if (!data.mimeType.equals(ContactsContract.CommonDataKinds.Photo.MIMETYPE)) {
                    values1.put(ContactsContract.Data.DATA15, data.data15);
                } else {
                    // 向data表插入头像数据
                    Bitmap sourceBitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.mipmap.ic_launcher);
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    // 将Bitmap压缩成PNG编码，质量为100%存储
                    sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    byte[] avatar = os.toByteArray();
                    values.put(ContactsContract.Data.DATA15, avatar);
                }
                Log.i(TAG, "[addContacts2Db] insert data.mimetype = " + data.mimeType);
                resolver.insert(uri, values1);
            }
        }
    }

    /**
     * 往数据库中新增通话记录
     *
     * @param context
     * @param callRecordList
     */
    public void addCallLogs(Context context, List<CallRecord> callRecordList) {
        ContentValues values = new ContentValues();
        for (CallRecord callRecord : callRecordList) {
            values.clear();
            values.put(CallLog.Calls.CACHED_NAME, callRecord.getName());
            values.put(CallLog.Calls.NUMBER, callRecord.getPhoneNum());
            values.put(CallLog.Calls.TYPE, callRecord.getType());
            values.put(CallLog.Calls.DATE, callRecord.getDate());
            values.put(CallLog.Calls.DURATION, callRecord.getDuration());
            values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
            context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        }
    }

    /**
     * 批量添加通话记录到联系人
     *
     * @param context
     * @param list
     */
    private void batchAddCallLogs(Context context, List<CallRecord> list) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values = new ContentValues();
        for (CallRecord callRecord : list) {
            values.clear();
            values.put(CallLog.Calls.CACHED_NAME, callRecord.getName());
            values.put(CallLog.Calls.NUMBER, callRecord.getPhoneNum());
            values.put(CallLog.Calls.TYPE, callRecord.getType());
            values.put(CallLog.Calls.DATE, callRecord.getDate());
            values.put(CallLog.Calls.DURATION, callRecord.getDuration());
            values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
            ops.add(ContentProviderOperation
                    .newInsert(CallLog.Calls.CONTENT_URI).withValues(values)
                    .withYieldAllowed(true).build());
        }
        if (ops != null) {
            try {
                // 真正添加
                ContentProviderResult[] results = context.getContentResolver().applyBatch(CallLog.AUTHORITY, ops);
                Log.i(TAG, "batchAddCallLogs -> results = " + results.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
        File file = new File(Environment.getExternalStorageDirectory() + filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public static ExecutorService getThreadPool() {
        return Executors.newCachedThreadPool();
    }

    public static String formartDate(long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        return simpleDateFormat.format(new Date(date));
    }

    public static String formartTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";

        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
