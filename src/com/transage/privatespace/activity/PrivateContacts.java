package com.transage.privatespace.activity;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.telephony.SmsApplication;
import com.transage.privatespace.PrivateSpaceApplication;
import com.transage.privatespace.R;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.fragment.BaseFragment;
import com.transage.privatespace.fragment.FG_PrivateCallRecords;
import com.transage.privatespace.fragment.FG_PrivateContacts;
import com.transage.privatespace.fragment.FG_PrivateSms;
import com.transage.privatespace.loader.OnLoadListener;
import com.transage.privatespace.loader.PrivateSpaceLoader;
import com.transage.privatespace.provider.PrivateObserver;
import com.transage.privatespace.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongrp on 2016/9/1. 此界面包含一个TabLayout布局、私密联系人、通话记录、短息
 * 3个Fragment,通过ViewPager进行切换
 */
public class PrivateContacts extends FragmentActivity implements View.OnClickListener, OnPageChangeListener, OnLoadListener {
    private static final String TAG= "PrivateContacts_Private";
    private static final String URI_PRIVATE_SPACE= "content://com.transage.privatespace";

    public static final int ADD_CONTACTS_REQUEST_CODE = 0x30;

    private List<BaseFragment> listFragment = new ArrayList<BaseFragment>(); // fragment集合
    private List<String> listTitle = new ArrayList<String>(); // tab名称集合
    private List<TextView> listTab = new ArrayList<TextView>(); // tab集合
    private ViewPager viewPager;
    private PrivateSpaceLoader mLoader;
    private ContentObserver mObserver;
    //默认短信
    public String mDefaultSmsApp = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_contacts);
        mLoader = new PrivateSpaceLoader(this,getApplicationContext());
        //注册内容观察者，监听PrivateProvider
        if (mObserver == null){
            mObserver = new PrivateObserver(mLoader.getmHandler());
            getContentResolver().registerContentObserver(Uri.parse(URI_PRIVATE_SPACE), true, mObserver);
        }
        mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(PrivateContacts.this);//获取系统默认短信
        initData();
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG,"[onActivityResult] requestCode = "+ requestCode);
        if (resultCode == RESULT_OK && data!=null){
            List<ContactInfo> peopleList = (List<ContactInfo>)data.getSerializableExtra(AddContacts.SELECTED_PEOPLE);
            if (peopleList != null && peopleList.size() > 0){
                mLoader.loadPrivateContacts(peopleList, 1, this);
                listFragment.get(LoadTag.CONTACT.getValue()).addData(peopleList);
                //通知栏显示进度
                NotificationUtils.getInstance(this).showNotification(NotificationUtils.ADD_CONTACT_NOTITY_ID, peopleList.size());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        //取消注册内容观察者，监听PrivateProvider
        getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroy();
    }
    //恢复默认短信
    public void setRestoreDefaultSms(Context context){
        Log.e("wangmeng","###setRestoreDefaultSms###### "+mDefaultSmsApp);
        SmsApplication.setDefaultApplication(mDefaultSmsApp, context);
    }
    /**
     * 初始化data
     */
    private void initData() {
        // 初始化各fragment 并将fragment存放集合listFragment中
        BaseFragment fg_PrivateCallRecords = new FG_PrivateCallRecords();// 私密通话记录
        BaseFragment fg_PrivatePeople = new FG_PrivateContacts();// 私密联系人
        BaseFragment fg_PrivateSms = new FG_PrivateSms();// 私密短信
        //添加加载器
        fg_PrivateCallRecords.setLoader(mLoader);
        fg_PrivatePeople.setLoader(mLoader);
        fg_PrivateSms.setLoader(mLoader);
        listFragment.add(fg_PrivateCallRecords);
        listFragment.add(fg_PrivatePeople);
        listFragment.add(fg_PrivateSms);
        // 初始化tab名字，并将名字存放进集合listTitle中
        listTitle.add(getString(R.string.call_record));
        listTitle.add(getString(R.string.people));
        listTitle.add(getString(R.string.sms));
        // 初始化tab ，并将tab存放进集合listTab中
        TextView tab1 = (TextView) findViewById(R.id.tab1);
        TextView tab2 = (TextView) findViewById(R.id.tab2);
        TextView tab3 = (TextView) findViewById(R.id.tab3);
        listTab.add(tab1);
        listTab.add(tab2);
        listTab.add(tab3);
    }

    /**
     * 初始化view
     */
    private void initView() {
        int tabPositions = getIntent().getIntExtra("tabPosition", 2);//Main传递过来的tabPosition
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        //noinspection deprecation
        viewPager.setOnPageChangeListener(this); //注册PageChangeListener
        // 创建FragmentPagerAdapter（适用于：ViewPager中放的是Fragment）
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return listFragment.get(position);
            }

            @Override
            public int getCount() {
                return listFragment.size();
            }

        };
        // viewPager绑定适配器fragmentPagerAdapter
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(tabPositions); //进入后显示的Fragment由tabPositions决定
        setSelectedTab(tabPositions);//根据tabPositions设置tab
    }


    /**
     * 设置选中的tab
     */
    public void setSelectedTab(int position) {
        for (int i = 0; i < 3; i++) {
            listTab.get(i).setTextColor(ContextCompat.getColor(this, R.color.white));
            listTab.get(i).setTextSize(14);
        }

        listTab.get(position).setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        listTab.get(position).setTextSize(16);
    }

    // ViewPager滑动时候回调的3个方法
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        setSelectedTab(position);
    }

    //点击监听
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab1:
                setSelectedTab(0);
                viewPager.setCurrentItem(0);
                break;
            case R.id.tab2:
                setSelectedTab(1);
                viewPager.setCurrentItem(1);
                break;
            case R.id.tab3:
                setSelectedTab(2);
                viewPager.setCurrentItem(2);
                break;
        }
    }

    @Override
    public void onLoadComplete(LoadTag loadTag, int action, int index) {
        Log.d(TAG, "onLoadComplete loadtag = " + loadTag.toString());
        listFragment.get(loadTag.getValue()). updateProcess(action, index);
    }
}
