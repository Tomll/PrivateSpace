package com.transage.privatespace.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Toast;

import com.transage.privatespace.R;
import com.transage.privatespace.adapter.AddContactListAdapter;
import com.transage.privatespace.adapter.HeaderViewRecyclerAdapter;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.utils.ContactUtils;
import com.transage.privatespace.view.EndlessRecyclerOnScrollListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dongrp on 2016/9/2. 添加联系人界面
 */
public class AddContacts extends BaseActivity implements AddContactListAdapter.OnItemClickLitener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    public static final String TAG = "AddContactTag";

    public static final String SELECTED_PEOPLE = "selected_people";

    private List<ContactInfo> mContactInfos = new ArrayList<ContactInfo>(); // 存放：所有未添加到私密空间的联系人
    private List<ContactInfo> mContactInfosCache = new ArrayList<ContactInfo>(); // 存放：所有未添加到私密空间的联系人
    // 该map记录最终选中的people
    private HashMap<Integer, ContactInfo> map_selectedPeople = new HashMap<Integer, ContactInfo>();
    // 将上面的map_selectedPeople中的值遍历，存入该list_selectedPeople集合
    private List<ContactInfo> list_selectedPeople = new ArrayList<ContactInfo>();
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private AddContactListAdapter mDataAdapter;
    private HeaderViewRecyclerAdapter mHeaderAdapter;

    //查询联系人工具
    private ContactUtils mContactUtils;
    //分页查询索引
    private int dataCounts = 0;
    //加载数据条数
    private int pageSize = 10;
    //加载数据偏移量
    private int currentOffset = 0;
    //是否重新加载数据
    private boolean isOverLoad = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        // 检查并申请READ_CONTACTS权限
        if (ContextCompat.checkSelfPermission(AddContacts.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddContacts.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1);
            return;
        }
        initViewAndAdapter();
        initData();
    }

    //权限申请结果的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initData();
            initViewAndAdapter();
        } else if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Iterator<Map.Entry<Integer, ContactInfo>> iterator = map_selectedPeople.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, ContactInfo> next = iterator.next();
                list_selectedPeople.add(next.getValue());
            }
            //将peopleList中的联系人加为私密
//            PrivateSpaceUtils.executePrivateContacts(list_selectedPeople, 1, this);
            if (list_selectedPeople.size() > 0) {
                Intent intent = new Intent();
                intent.putExtra(SELECTED_PEOPLE, (Serializable) list_selectedPeople);
                setResult(RESULT_OK, intent);
            }
//            setPrivateContacts(list_selectedPeople, 1, getContentResolver());
            finish();
        }
    }

    /**
     * 初始化数据（获取未添加到私密空间的联系人：is_private_contacts = 0）
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initData() {
        loadDatabyTask();
    }

    /**
     * 初始化View 和 Adapter
     */
    private void initViewAndAdapter() {
        //获取下拉刷新控件
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.add_contacts_recycler_swipe);
        //设置下拉刷新监听器
        mSwipeLayout.setOnRefreshListener(this);
//        mSwipeLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        //设置刷新进度条颜色
        mSwipeLayout.setColorSchemeResources(R.color.colorAccent);

        mRecyclerView = (RecyclerView) findViewById(R.id.add_contacts_recycler_view);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //tag 上拉加载下拉刷新功能**************************************start
        //创建适配器
        mDataAdapter = new AddContactListAdapter(this);
        //创建一个可添加尾部头部布局的RecycleViewAdapter
        mHeaderAdapter = new HeaderViewRecyclerAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mHeaderAdapter);
        createLoadMoreView();

        //设置点击事件监听器
        mDataAdapter.setOnItemClickLitener(this);
        //设置adapter
//        mRecyclerView.setAdapter(mDataAdapter);
        //设置滚动监听器
        mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                loadMore(currentPage);
            }
        });
        //tag 上拉加载下拉刷新功能**************************************end

        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * 创建加载更多选项
     */
    private void createLoadMoreView() {
        if (mHeaderAdapter.footerViewCount() == 0){
            //加载list尾部布局
            View loadMoreView = LayoutInflater
                    .from(this)
                    .inflate(R.layout.recycle_view_footer, mRecyclerView, false);
            //添加一个尾部布局
            mHeaderAdapter.addFooterView(loadMoreView);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (!mContactInfos.get(position).isPrivateEnable()) {
            Toast.makeText(this, getResources().getText(R.string.toast_sim_nonsupport_private), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(mContactInfos.get(position).getHasPhoneNumber() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.toast_no_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }

        AddContactListAdapter.AddHolder viewHolder = (AddContactListAdapter.AddHolder) view.getTag();
        viewHolder.checkBox.toggle(); // 置反
        AddContactListAdapter.map_allCheckBoxSelectedStatus.put(position, viewHolder.checkBox.isChecked());// 更新适配器中的map
        if (viewHolder.checkBox.isChecked()) { // 将选中的people记录到map_selectedPeople中
            map_selectedPeople.put(position, mContactInfos.get(position));
        } else {
            map_selectedPeople.remove(position);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    // 点击监听回调
    @Override
    public void onClick(View v) { // 只有一个“完成”按钮，所以就不做switch了
        // 检查并申请WRITE_EXTERNAL_STORAGE权限
        if (ContextCompat.checkSelfPermission(AddContacts.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddContacts.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            return;
        }
        list_selectedPeople.clear();
        Iterator<Map.Entry<Integer, ContactInfo>> iterator = map_selectedPeople.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ContactInfo> next = iterator.next();
            Log.i(TAG, "next.getValue().toString() = " + next.getValue().toString());
            list_selectedPeople.add(next.getValue());
        }
        //将peopleList中的联系人加为私密
//        setPrivateContacts(list_selectedPeople, 1, getContentResolver());
//        PrivateSpaceUtils.executePrivateContacts(list_selectedPeople, 1, this);
        if (list_selectedPeople.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra(SELECTED_PEOPLE, (Serializable) list_selectedPeople);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    /**
     * 下拉操作触发，刷新数据
     */
    @Override
    public void onRefresh() {
        Log.i(TAG, "[onRefresh]");
        isOverLoad = true;
        createLoadMoreView();
        //将查询偏量设置到初始位置
        currentOffset = 0;
        loadDatabyTask();
    }

    /**
     * 上拉操作触发，加载更多数据
     *
     * @param currentPage
     */
    public void loadMore(int currentPage) {
        Log.i(TAG, "[onLoadMore]");
        isOverLoad = false;
        if (hasLoadMore()){
            loadDatabyTask();
            return;
        }
        mHeaderAdapter.removeFooterView();
        Toast.makeText(this, getResources().getText(R.string.no_load_more), Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断有没有更多数据可提供加载
     * @return
     */
    private boolean hasLoadMore(){
        return currentOffset < dataCounts;
    }

    /**
     * 通过任务加载数据
     */
    private void loadDatabyTask() {
        try {
            GetContactTask task = new GetContactTask();
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GetContactTask extends AsyncTask<Void, Integer, Integer> {
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
            if (mContactUtils == null) {
                mContactUtils = new ContactUtils(AddContacts.this);
            }

//            mContactInfos = contactUtils.getContacts();
            //获取分页数据
            dataCounts = mContactUtils.getAllCounts();
            Log.i(TAG, "[GetContactTask] dataCounts = " + dataCounts + " pageSize = " + pageSize + " currentOffset = " + currentOffset);
            if (hasLoadMore()) {
                mContactInfosCache = mContactUtils.getContactsByPage(pageSize, currentOffset);
                currentOffset += pageSize;
            }

            return null;
        }

        /**
         * 运行在ui线程中，在doInBackground()执行完毕后执行
         */
        @Override
        protected void onPostExecute(Integer integer) {
            //关闭刷新进度条
            mSwipeLayout.setRefreshing(false);
            if (isOverLoad){
                mContactInfos.clear();
            }
            mContactInfos.addAll(mContactInfosCache);
            mDataAdapter.setData(mContactInfosCache, isOverLoad);

            //如果数据不满一页，隐藏页脚加载更多item
            if (dataCounts <= pageSize){
                mHeaderAdapter.removeFooterView();
            }
        }

        /**
         * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    /**
     * 更新联系人数据信息：将选中的联系人设置为私密联系人
     *
     * @param list        ：待加密/解密的联系人列表
     * @param privateFlag ：1表示加为私密 ，0表示解除私密
     * @param cr          ：内容解析者
     */
//    public static void setPrivateContacts(ArrayList<People> list, int privateFlag, ContentResolver cr) {
//        ContentValues values = new ContentValues();
//        values.put(ContactsContract.Contacts.IS_PRIVATE_CONTACTS, privateFlag);
//        String[] selectionArgs = new String[1];
//        for (int i = 0; i < list.size(); i++) {
//            selectionArgs[0] = String.valueOf(list.get(i).getRawContactId());
//            cr.update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.NAME_RAW_CONTACT_ID + "= ?", selectionArgs);
//        }
//    }
}
