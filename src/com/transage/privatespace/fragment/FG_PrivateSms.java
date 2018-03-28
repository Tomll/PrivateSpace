package com.transage.privatespace.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.transage.privatespace.R;
import com.transage.privatespace.activity.PrivateSms;
import com.transage.privatespace.adapter.SmsListViewAdapter;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.bean.SmsInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.loader.PrivateSpaceLoader;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dongrp on 2016/9/1.
 */
public class FG_PrivateSms extends BaseFragment implements SmsListViewAdapter.OnItemClickLitener,SwipeRefreshLayout.OnRefreshListener{
    private View view;
    private List<SmsInfo> listSmslist = new ArrayList<SmsInfo>();
    private SmsListViewAdapter smsListViewAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    //private DatabaseAdapter mDb;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sms, null);
        //mDb = new DatabaseAdapter(getContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 检查并申请READ_SMS权限
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_SMS}, 1);
//            return;
//        }
//        initData();
//        initViewAndAdapter();
    }

    //READ_SMS权限申请结果的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initData();
            initViewAndAdapter();
        }
    }

    /**
     * 初始化组件适配器
     */
    public void initViewAndAdapter() {
        //获取下拉刷新控件
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_sms_swipe);
        //设置下拉刷新监听器
        mSwipeLayout.setOnRefreshListener(this);
        //设置刷新进度条颜色
        mSwipeLayout.setColorSchemeResources(R.color.colorAccent);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_sms_list);
//        final ListView listView = (ListView) view.findViewById(R.id.listViewSms);
        Collections.sort(listSmslist, new Comparator<SmsInfo>() {
            public int compare(SmsInfo smsInfo1, SmsInfo smsInfo2) {
                Date date1 = stringToDate(smsInfo1.getDate());
                Date date2 = stringToDate(smsInfo2.getDate());
                Log.e("wangmeng","----date1------"+date1);
                Log.e("wangmeng","----date2------"+date2);
                if(date1.before(date2)){
                    return 1;
                }
                return -1;
            }
        });
        smsListViewAdapter = new SmsListViewAdapter(getActivity(), listSmslist);
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        smsListViewAdapter.setOnItemClickLitener(this);
        //设置adapter
        mRecyclerView.setAdapter(smsListViewAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        listView.setAdapter(smsListViewAdapter);
//        smsListViewAdapter.notifyDataSetChanged();
//                setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String address = listSmslist.get(i).getAddress();
//                String person = listSmslist.get(i).getPerson();
//                Intent intent = new Intent();
//                intent.putExtra("address",address);
//                intent.putExtra("person",person);
//                intent.setClass(getActivity(), PrivateSms.class);
//                Log.e("wangmeng", "onItemClick: " +address);
//                startActivity(intent);
//            }
//        });
    }



    /**
     * 初始化数据
     */
    public void initData() {
        listSmslist.clear();
        ArrayList<ContactInfo> listPrivatePeople = new ArrayList<>();
        listPrivatePeople.addAll(new DatabaseAdapter(getActivity()).getContacts(null, null));
        for(int i = 0;i<listPrivatePeople.size();i++){
//            Log.e("wangmeng","-----SmsNumberlist-----"+listPrivatePeople.get(i).toString());
            listSmslist.addAll(new DatabaseAdapter(getActivity()).
                    getLastSms(formatNumber(listPrivatePeople.get(i).getPhones().get(0).phoneNumber)));
        }

//
    }

    @Override
    public void setLoader(PrivateSpaceLoader loader) {
        mLoader = loader;
    }

    @Override
    public void updateProcess(int action, int index) {
        initData();
        initViewAndAdapter();
    }
    //格式化电话号码 add by wangmeng 20170821
    private String formatNumber(String number) {
        String phoneNumStr = null;
        if (number.contains("+86")) {
            phoneNumStr = number.replace("+86", "").replaceAll(" ", "").replaceAll("-", "").trim();
        } else {
            phoneNumStr = number.replaceAll(" ", "").replaceAll("-", "").trim();
        }
        return phoneNumStr;
    }
    public static Date stringToDate(long dateString){
        ParsePosition position = new ParsePosition(0);
        Log.e("wangmeng","----dateString----"+dateString);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateStr = simpleDateFormat.format(new Date(dateString));
        Date dateValue = simpleDateFormat.parse(dateStr, position);
        Log.e("wangmeng","----dateStr----"+dateStr);
        Log.e("wangmeng","----dateValue----"+dateValue);
        return dateValue;
    }

    public void onRefresh() {
        initData();
        smsListViewAdapter.setData(listSmslist);
        Log.e("wangmeng", "[onRefresh] start mListPrivateContact.size = " + listSmslist.size());
        mSwipeLayout.setRefreshing(false);
    }

    public void onItemClick(View view, int position) {
        String address = listSmslist.get(position).getAddress();
        String person = listSmslist.get(position).getPerson();
        Intent intent = new Intent();
        intent.putExtra("address",address);
        intent.putExtra("person",person);
        intent.setClass(getActivity(), PrivateSms.class);
        Log.e("wangmeng", "onItemClick: " +address);
        startActivity(intent);
    }

    public void onItemLongClick(View view, int position) {

    }
//    public void initData() {
//        // 1.先获取到所有私密联系人
//        ArrayList<People> listPrivatePeople = new ArrayList<People>();
//        listPrivatePeople.addAll(new DatabaseAdapter(getContext()).getContacts());
//
//        // 2.获取私密联系人的短信记录
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
//        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
//        for (int i = 0; i < listPrivatePeople.size(); i++) {
//            String phone = listPrivatePeople.get(i).getPhoneNum();
//            if (phone != null){
//                Cursor cursor2 = getActivity().getContentResolver().query(Uri.parse("content://sms/"), projection,
//                        "address=?", new String[]{phone}, "date desc");
//                if (cursor2 != null && cursor2.moveToFirst()) {
//                    ArrayList<Sms> listSms = new ArrayList<Sms>();
//                    do {
//                        String address = cursor2.getString(cursor2.getColumnIndex("address"));// 手机号码
//                        String body = cursor2.getString(cursor2.getColumnIndex("body"));// 短信内容
//                        String date = simpleDateFormat.format(new Date(cursor2.getLong(cursor2.getColumnIndex("date"))));// 收发时间
//                        int type = cursor2.getInt(cursor2.getColumnIndex("type"));// 收发类型
//                        // 添加一条短息数据到listSms
//                        Sms sms = new Sms();
//                        sms.setAddress(address);
//                        sms.setPerson(listPrivatePeople.get(i).getName());
//                        sms.setBody(body);
//                        sms.setDate(date);
//                        sms.setType(type);
//                        listSms.add(sms);
//                    } while (cursor2.moveToNext());
//                    listSmslist.add(listSms);
//                    cursor2.close();
//                }
//            }
//        }
//    }

}
