package com.transage.privatespace.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transage.privatespace.R;
import com.transage.privatespace.activity.CallRecordDetailActivity;
import com.transage.privatespace.adapter.CallRecordListAdapter;
import com.transage.privatespace.bean.CallRecordInfo;
import com.transage.privatespace.bean.CallRecord;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.database.PsDatabaseHelper;
import com.transage.privatespace.loader.PrivateSpaceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongrp on 2016/9/1.
 */
public class FG_PrivateCallRecords extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, CallRecordListAdapter.OnItemClickLitener{
    private static final String TAG = "FG_PrivateCallRecords";
    private View view;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    private CallRecordListAdapter mAdapter;
    private DatabaseAdapter mDb;
    //    private ArrayList<ArrayList<CallRecord>> listCallRecordList = new ArrayList<ArrayList<CallRecord>>(); // 该list存放每个私密联系人的通话记录
    private List<CallRecordInfo> list_callRecord = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_call_records, null);
        mDb = new DatabaseAdapter(getActivity());
        initData();
        initViewAndAdapter();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null){
            String action = data.getAction();
            //执行删除任务
            if (CallRecordDetailActivity.DELETE_CALL.equals(action)){
                String phone = data.getStringExtra(PsDatabaseHelper.CallRecordClumns.NUMBER);
                for (int i=0; i<list_callRecord.size(); i++){
                    if (phone.equals(list_callRecord.get(i).phone())){
                        //删除内存中的通话记录
//                        list_callRecord.remove(i);
//                        mAdapter.setData(list_callRecord);
                        //删除数据库中通话记录
                        mDb.deleteCallRecordByNum(phone);
                        //刷新数据
                        mSwipeLayout.setRefreshing(true);
                        initData();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化view 和 adapter
     */
    private void initViewAndAdapter() {
        //获取下拉刷新控件
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_call_swipe);
        //设置下拉刷新监听器
        mSwipeLayout.setOnRefreshListener(this);
//        mSwipeLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        //设置刷新进度条颜色
        mSwipeLayout.setColorSchemeResources(R.color.colorAccent);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_call_list);
        //创建适配器
        mAdapter = new CallRecordListAdapter(getContext());
        //设置点击事件监听器
        mAdapter.setOnItemClickLitener(this);
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //设置adapter
        mRecyclerView.setAdapter(mAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

//        callRecordListViewAdapter = new CallRecordListViewAdapter(getActivity(), list_callRecord);
//        listView.setAdapter(callRecordListViewAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ContactListAdapter.callPhoneOrSendSms(getActivity(), 1, list_callRecord.get(position).phone());
//            }
//        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getContext(), CallRecordDetailActivity.class);
        intent.putExtra("call_record_info", list_callRecord.get(position));
        startActivityForResult(intent, 10);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    /**
     * 初始化数据（获取私密联系人：is_private_contacts = 1）
     */
    private void initData() {
        try {
            CallTask task = new CallTask();
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CallTask extends AsyncTask<Void, Integer, Integer> {
        /**
         * 运行在UI线程中，在调用doInBackground()之前执行
         */
        @Override
        protected void onPreExecute() {
        }

        /**
         * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
         */
        @Override
        protected Integer doInBackground(Void... params) {
            list_callRecord.clear();
            List<CallRecord> callRecords = mDb.getCallRecords();
            Log.i(TAG, "callRecords.size = " + callRecords.size());
            CallRecordInfo info = new CallRecordInfo();
            for (int i = 0; i<callRecords.size(); i++) {
                CallRecord call = callRecords.get(i);
                if (info.exists(call.getPhoneNum())){
                    info.addCallRecord(call);
                }else {
                    info = new CallRecordInfo();
                    info.addCallRecord(call);
                    //获取头像图片信息
                    byte[] photoData = mDb.getPhotoDataById(call.getPhotoFileId());
                    info.setPhotoData(photoData);
                    list_callRecord.add(info);
                }
            }
            return null;
        }

        /**
         * 运行在ui线程中，在doInBackground()执行完毕后执行
         */
        @Override
        protected void onPostExecute(Integer integer) {
            mAdapter.setData(list_callRecord);
            mSwipeLayout.setRefreshing(false);
        }

        /**
         * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    @Override
    public void setLoader(PrivateSpaceLoader loader) {
        mLoader = loader;
    }

    @Override
    public void onRefresh() {
        initData();
    }

    @Override
    public void updateProcess(int action, int index) {
        initData();
    }
}
