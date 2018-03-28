package com.transage.privatespace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.ContactDataType;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.database.PsDatabaseHelper;
import com.transage.privatespace.utils.CallUtils;
import com.transage.privatespace.view.CircleImageView;

import java.util.List;

/**
 * 联系人详情Activity
 * Created by yanjie.xu on 2017/9/16.
 */

public class ContactDetailActivity extends AppCompatActivity {
    private static final String TAG = "ContactDetailActivity";
    public static final String DELETE_CONTACT = "action.delete.contact";

    private int mContactId;
    private ContactInfo mContact;
    private DatabaseAdapter mDb;

    private CircleImageView mPhoto;
    private TextView mName;
    private View mLineSplit;
    private Toolbar mToolBar;

    //联系人账户信息容器
    private LinearLayout mInfoContainer;
    //个人简介信息容器
    private LinearLayout mPresentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        mContactId = getIntent().getIntExtra(PsDatabaseHelper.ContactsColumns._ID, -1);
        initData();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_detail_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.call_detail_menu_action_delete).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    private void initData() {
        if (mDb == null) {
            mDb = new DatabaseAdapter(this);
        }
        if (mContactId > 0) {
            mContact = mDb.getContactById(mContactId);
        }
    }

    private void initView() {
        mPhoto = (CircleImageView) findViewById(R.id.contact_details_photo);
        mName = (TextView) findViewById(R.id.contact_details_name);
        mToolBar = (Toolbar) findViewById(R.id.contact_detail_toolbar);

        //toolbar设置
        //设置导航图标要在setSupportActionBar方法之后
        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        getSupportActionBar().setTitle("");
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.call_detail_menu_action_delete:
                        AlertDialog dialog = new AlertDialog.Builder(ContactDetailActivity.this)
                                .setTitle(getString(R.string.delete_contact_title))
                                .setMessage(getString(R.string.confirm_delete_contact))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        Toast.makeText(CallRecordDetailActivity.this, "action_delete",
//                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent();
                                        intent.setAction(DELETE_CONTACT);
                                        intent.putExtra(PsDatabaseHelper.ContactsColumns._ID, mContactId);
                                        setResult(RESULT_OK, intent);
                                        ContactDetailActivity.this.finish();
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), null)
                                .show();
                        break;
                }
                return true;
            }
        });
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactDetailActivity.this.finish();
            }
        });

        mInfoContainer = (LinearLayout) findViewById(R.id.contact_details_info_rootview);
        mPresentContainer = (LinearLayout) findViewById(R.id.contact_details_present_rootview);
        mPresentContainer.setVisibility(View.GONE);
        List<ContactInfo.RawContactInfo> rawContactInfos = mContact.getRawContactInfos();
        for (ContactInfo.RawContactInfo rawContactInfo : rawContactInfos) {
            for (ContactInfo.DataInfo dataInfo : rawContactInfo.dataInfos) {
                bindView(dataInfo, rawContactInfo.accountType, rawContactInfo.accountName);
            }
        }

        if (mContact.getPhoto() != null) {
            mPhoto.setImageBitmap(mContact.getPhoto());
        }

        mPhoto.setBorderWidth(2);
        mPhoto.setBorderColor(0xFFFFFFFF);

        mName.setText(mContact.getDisplayName());

//        mInfoContainer.setEnabled(true);

        setClickAction();
    }

    /**
     * 设置点击事件
     */
    private void setClickAction() {
        int childCount = mInfoContainer.getChildCount();
        Log.d(TAG, "[setClickAction] childCount = " + childCount);
        for (int i = 0; i < childCount; i++) {
            View childAt = mInfoContainer.getChildAt(i);
            mInfoContainer.setClickable(true);
            childAt.setEnabled(true);
            childAt.setClickable(true);
            ContactInfo.DataInfo tag = (ContactInfo.DataInfo) childAt.getTag();
            Log.d(TAG, "[setClickAction] tag = " + tag.toString());
            childAt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "[setClickAction] setOnClickListener" );
                    // 1.2 获取各种电话信息
                    if (Phone.CONTENT_ITEM_TYPE.equals(tag.mimeType)) {
                        //电话号码类型，转到打电话
                        CallUtils.callPhoneOrSendSms(ContactDetailActivity.this, 1, tag.data1);// 打电话
                    }

                    //查找Email信息
                    if (Email.CONTENT_ITEM_TYPE.equals(tag.mimeType)) {
                        //电话号码类型，转到发邮箱
                        String[] emails = new String[]{tag.data1};
                        CallUtils.sendEmail(ContactDetailActivity.this, emails);
                    }

                    if (PsDatabaseHelper.WhatsApp.PROFILE_TYPE.equals(tag.mimeType)) {

                    }

                }
            });
        }
    }

    private void bindView(ContactInfo.DataInfo dataInfo, String accountType, String accountName) {
        mLineSplit = View.inflate(this, R.layout.line_split, null);
        Log.d(TAG, "[bindView] accountType = " + accountType + "accountName = " + accountName);
        Log.d(TAG, "[bindView] dataInfo = " + dataInfo.toString());
        String mimetype = dataInfo.mimeType;

//        if ("Local Phone Account".equals(accountType)){
        if ("Phone".equals(accountName)) {
            // 1.2 获取各种电话信息
            if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
                mInfoContainer.addView(bindInfoItemView(dataInfo, ContactDataType.PHONE_TYPE, accountType));
            }

//        mInfoContainer.addView(mLineSplit);
            //查找Email信息
            if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
                mInfoContainer.addView(bindInfoItemView(dataInfo, ContactDataType.EMAIL_TYPE, accountType));
            }
        }

        if ("com.whatsapp".equals(accountType)) {
            if (PsDatabaseHelper.WhatsApp.PROFILE_TYPE.equals(mimetype)) {
                mInfoContainer.addView(bindInfoItemView(dataInfo, ContactDataType.OTHER_TYPE, accountType));
            }
        }

//        // 查找event地址
//        if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出时间类型
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
//        }
//        // 获取即时通讯消息
//        if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出即时消息类型
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
//        }
//        // 获取备注信息
//        if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
//        }
//        // 获取昵称信息
//        if (Nickname.CONTENT_ITEM_TYPE.equals(mimetype)) {
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
//        }
//        // 获取组织信息
//        if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出组织类型
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
////            dataInfo.data1 = cursor.getString(cursor
////                    .getColumnIndex(Organization.COMPANY));
////            dataInfo.data4 = cursor.getString(cursor
////                    .getColumnIndex(Organization.TITLE));
////            dataInfo.data5 = cursor.getString(cursor
////                    .getColumnIndex(Organization.DEPARTMENT));
//        }
//        // 获取网站信息
//        if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出组织类型
//            mPresentContainer.addView(bindPresentItemView(dataInfo));
//        }
//        // 查找通讯地址
//        if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) { // 取出邮件类型
//            dataInfo.data4 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.STREET));
//            dataInfo.data7 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.CITY));
//            dataInfo.data5 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.POBOX));
//            dataInfo.data6 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.NEIGHBORHOOD));
//            dataInfo.data8 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.REGION));
//            dataInfo.data9 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.POSTCODE));
//            dataInfo.data10 = cursor.getString(cursor
//                    .getColumnIndex(StructuredPostal.COUNTRY));
//        }
//        //查找头像数据
//        if (Photo.CONTENT_ITEM_TYPE.equals(mimetype)) {
//            dataInfo.data14 = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));
//            byte[] buff = cursor.getBlob(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
//            if (buff != null) {
//                dataInfo.data15 = Base64.encodeToString(buff, Base64.DEFAULT);
//            }
//        }
    }

    /**
     * 创建infoitemview
     *
     * @param dataInfo
     * @return
     */
    @NonNull
    private View bindInfoItemView(ContactInfo.DataInfo dataInfo, int dataType, String accountType) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_contact_detail_info, null);
        TextView data1 = (TextView) item.findViewById(R.id.contact_detail_info_data1);
        TextView type = (TextView) item.findViewById(R.id.contact_detail_info_type);
        ImageView image = (ImageView) item.findViewById(R.id.contact_detail_info_image);
        int imageRes = -1;

        switch (dataType) {
            case ContactDataType.PHONE_TYPE:
                imageRes = android.R.drawable.ic_menu_call;
                break;
            case ContactDataType.EMAIL_TYPE:
                imageRes = android.R.drawable.ic_dialog_email;
                break;
            case ContactDataType.OTHER_TYPE:
                imageRes = -1;
                break;
        }

        if (imageRes > 0) {
            image.setImageResource(imageRes);
        } else {
            image.setImageDrawable(getAppIcon(accountType));
        }

        data1.setText(dataInfo.data1);
        type.setText(dataInfo.typeName);

        item.setTag(dataInfo);

//        data1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "[bindInfoItemView] data1.setOnClickListener");
//            }
//        });

//        clickview.setClickable(true);
//        clickview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "[bindInfoItemView] clickview.setOnClickListener");
//            }
//        });
//        item.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "[bindInfoItemView] ContactDataType.PHONE_TYPE = " + ContactDataType.PHONE_TYPE);
//                switch (dataType) {
//                    case ContactDataType.PHONE_TYPE:
//                        //电话号码类型，转到打电话
//                        CallUtils.callPhoneOrSendSms(ContactDetailActivity.this, 1, dataInfo.data1);// 打电话
//                        break;
//                    case ContactDataType.EMAIL_TYPE:
//                        //电话号码类型，转到发邮箱
//                        String[] emails = new String[]{dataInfo.data1};
//                        CallUtils.sendEmail(ContactDetailActivity.this, emails);
//                        break;
//                    case ContactDataType.OTHER_TYPE:
//                        break;
//                }
//            }
//        });
        return item;
    }

    /**
     * 创建presentitemview
     *
     * @param dataInfo
     * @return
     */
    @NonNull
    private View bindPresentItemView(ContactInfo.DataInfo dataInfo) {
        View item = View.inflate(this, R.layout.item_contact_detail_present, null);
        TextView data1 = (TextView) item.findViewById(R.id.contact_detail_present_data1);
        TextView type = (TextView) item.findViewById(R.id.contact_detail_present_type);
        data1.setText(dataInfo.data1);
        type.setText(dataInfo.typeName);
        return item;
    }

    /**
     * 根据包名获取应用图标
     *
     * @param packge
     * @return
     */
    private Drawable getAppIcon(String packgename) {
        PackageManager pm = getApplication().getPackageManager();
        Drawable drawable = null;
        try {
            drawable = pm.getApplicationIcon(packgename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

}
