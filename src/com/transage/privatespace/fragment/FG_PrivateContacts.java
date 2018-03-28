package com.transage.privatespace.fragment;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.transage.privatespace.R;
import com.transage.privatespace.activity.AddContacts;
import com.transage.privatespace.activity.ContactDetailActivity;
import com.transage.privatespace.activity.PrivateContacts;
import com.transage.privatespace.adapter.ContactListAdapter;
import com.transage.privatespace.adapter.HeaderViewRecyclerAdapter;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.database.PsDatabaseHelper;
import com.transage.privatespace.loader.PrivateSpaceLoader;
import com.transage.privatespace.utils.ContactUtils;
import com.transage.privatespace.utils.NotificationUtils;
import com.transage.privatespace.view.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dongrp on 2016/9/1.
 */
public class FG_PrivateContacts extends BaseFragment implements ContactListAdapter.OnItemClickLitener,
        SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "FG_PrivateContacts";
    private static final int CREATE_CONTACT = 0x15;
    private static final int CALL_CONTACT_DETAIL = 0x16;

    private View view;
    private Dialog mDialog;
    private ImageButton floatActionButton;
    private boolean isShowCheckBox = false;
    private DatabaseAdapter mDb;
    private ContactUtils mContactUtils;
    //通知栏对象
    private NotificationUtils mNotify;
    private RecyclerView mRecyclerView;
    private ContactListAdapter mDataAdapter;
    //分页查询索引
    private int dataCounts = -1;
    //加载数据条数
    private int pageSize = 10;
    //加载数据偏移量
    private int currentOffset = 0;
    //是否重新加载数据
    private boolean isOverLoad = true;
    //可添加头和脚的Recycle适配器
    private HeaderViewRecyclerAdapter mHeaderAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private List<ContactInfo> mListPrivateContact = new ArrayList<ContactInfo>();
    private List<ContactInfo> mListContactCache = new ArrayList<ContactInfo>();
    public static HashMap<Integer, ContactInfo> mapSelectedContact = new HashMap<Integer, ContactInfo>();
    public static ArrayList<ContactInfo> listSelectedContact = new ArrayList<ContactInfo>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mDb = new DatabaseAdapter(getContext());
        mContactUtils = new ContactUtils(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts, null);
        // 获取焦点、设置setOnKeyListener，用于在Fragment监听back按钮点击事件
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(backPressListener);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            //新建联系人
            if (requestCode == CREATE_CONTACT) {
                long contactId = ContentUris.parseId(data.getData());
                List<ContactInfo> contacts = mContactUtils.getContacts(contactId);
                if (contacts.size() > 0) {
                    ContactInfo contact = contacts.get(contacts.size() - 1);
                    if (!(contact.isPrivateEnable())) {
                        Toast.makeText(getContext(), R.string.toast_sim_nonsupport_private, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(contact.getHasPhoneNumber() > 0)) {
                        Toast.makeText(getContext(), R.string.toast_no_phone_number, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<ContactInfo> cList = new ArrayList<>();
                    cList.add(contact);
                    //更新界面数据
                    mListPrivateContact.add(contact);
                    isOverLoad = true;
                    mDataAdapter.setData(mListPrivateContact, isOverLoad);
                    //更新数据库数据
                    mLoader.loadPrivateContacts(cList, 1, getActivity());
                    Log.d(TAG, "[onActivityResult] contacts.size = " + contacts.size() +
                            " contacts.toString() = " + contact.toString());
                }
            }

            //执行删除任务
            String action = data.getAction();
            Log.d(TAG, "[onActivityResult] action = " + action);
            if (ContactDetailActivity.DELETE_CONTACT.equals(action)) {
                int _id = data.getIntExtra(PsDatabaseHelper.ContactsColumns._ID, -1);
                if (_id > 0) {
                    //删除数据库中通话记录
                    mDb.deleteContactById(_id);
                    //刷新数据
//                    mSwipeLayout.setRefreshing(true);
                    onRefresh();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHeaderAdapter == null && dataCounts < 0) {
            initViewAndAdapter();// 绑定适配器
            initData();// 重新加载数据,保证获取最新联系人数据
        }
    }

    /**
     * 按键监听器（由于Fragment中不提供onBackPressed()重写，所以自己写监听器监听返回键）
     */
    private View.OnKeyListener backPressListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK) { // 表示按返回键 时的操作
                    if (isShowCheckBox) {
                        isShowCheckBox = false;
                        mDataAdapter.showCheckBox(false);
                        // 给floatActionButton添加旋转动画
                        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, -360.0f,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setFillAfter(true);
                        rotateAnimation.setDuration(500);
                        floatActionButton.startAnimation(rotateAnimation);
                    } else {
                        getActivity().finish();
                    }
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * 初始化View
     */
    private void initViewAndAdapter() {
        //获取下拉刷新控件
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_contacts_float_swipe);
        //设置下拉刷新监听器
        mSwipeLayout.setOnRefreshListener(this);
//        mSwipeLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        //设置刷新进度条颜色
        mSwipeLayout.setColorSchemeResources(R.color.colorAccent);
        // floatActionButton_add 和 floatActionButton_delete
        floatActionButton = (ImageButton) view.findViewById(R.id.fragment_contacts_float_button);
        floatActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowCheckBox) { // 批量解除私密联系人
                    if (NotificationUtils.getInstance(getContext()).isShow()){
                        Toast.makeText(getContext(), R.string.notification_sync_title, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listSelectedContact.clear();
                    Iterator<Map.Entry<Integer, ContactInfo>> iterator = mapSelectedContact.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, ContactInfo> next = iterator.next();
                        int key = next.getKey();
                        listSelectedContact.add(next.getValue());
                        mListPrivateContact.remove(next.getValue());
                        //更新本地list
//                        Log.i(TAG, "[onItemClick] key = " + key);
//                        if (key < mListPrivateContact.size()) {
//                            mListPrivateContact.remove(key);
//                        }
                    }
                    //通知栏显示进度
                    if (listSelectedContact.size() > 0) {
                        NotificationUtils.getInstance(getContext()).showNotification(NotificationUtils.REMOVE_CONTACT_NOTITY_ID, listSelectedContact.size());
                    }
                    //将listSelectedPeople中的联系人解除私密
//                    AddContacts.setPrivateContacts(listSelectedPeople, 0, getActivity().getContentResolver());
//                    PrivateSpaceUtils.executePrivateContacts(listSelectedPeople, 0, getActivity());
                    mLoader.loadPrivateContacts(listSelectedContact, 0, getActivity());
                    //更新list显示
                    isOverLoad = true;
                    mDataAdapter.setData(mListPrivateContact, isOverLoad);
                    //更新check按钮显示
                    isShowCheckBox = false;
                    mDataAdapter.showCheckBox(isShowCheckBox);
                    // 给floatActionButton添加旋转动画
                    RotateAnimation rotateAnimation = new RotateAnimation(0.0f, -360.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(500);
                    floatActionButton.startAnimation(rotateAnimation);
                } else { // 添加
                    showDialog();
                }
            }
        });
        // listView 及其 适配器绑定、事件监听
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_contacts_list);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        //创建适配器
        mDataAdapter = new ContactListAdapter(getContext());
        //创建一个可添加尾部头部布局的RecycleViewAdapter
        mHeaderAdapter = new HeaderViewRecyclerAdapter(mDataAdapter);
        //设置adapter
        mRecyclerView.setAdapter(mHeaderAdapter);
        createLoadMoreView();
        //设置点击事件监听器
        mDataAdapter.setOnItemClickLitener(this);
        //设置滚动监听器
        mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                loadMore(currentPage);
            }
        });

        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        recyclerView.addItemDecoration(new DividerItemDecoration(
//                getActivity(), DividerItemDecoration.HORIZONTAL_LIST));
    }

    /**
     * 创建并显示选择菜单
     */
    public void showDialog() {
        mDialog = new Dialog(getContext(), R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contacts, null);
        //初始化控件
        TextView choose = (TextView) inflate.findViewById(R.id.dialog_add_contacts_choose);
        TextView create = (TextView) inflate.findViewById(R.id.dialog_add_contacts_create);
        TextView cancel = (TextView) inflate.findViewById(R.id.dialog_add_contacts_cancel);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AddContacts.class),
                        PrivateContacts.ADD_CONTACTS_REQUEST_CODE);
                mDialog.dismiss();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
                addIntent.setType("vnd.android.cursor.dir/person");
                addIntent.setType("vnd.android.cursor.dir/contact");
                addIntent.setType("vnd.android.cursor.dir/raw_contact");
                //mFinishActivityOnSaveCompleted为ture则关闭该activity，否则就启动另一个查看联系人详情的activity。
                addIntent.putExtra("finishActivityOnSaveCompleted", true);
                addIntent.putExtra("com.android.contacts.extra.ACCOUNT", "Local Phone Account");
//            addIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, "110");
                startActivityForResult(addIntent, CREATE_CONTACT);
//            startActivity(addIntent);
                mDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        //将布局设置给Dialog
        mDialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = mDialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        mDialog.show();//显示对话框
    }

    @Override
    public void onItemClick(ContactListAdapter.ContactHolder viewHolder, int position) {
        if (isShowCheckBox) {
            viewHolder.checkBox.toggle();
            ContactListAdapter.map_allCheckBoxSelectedStatus.put(position, viewHolder.checkBox.isChecked());
            if (viewHolder.checkBox.isChecked()) {
                mapSelectedContact.put(position, mListPrivateContact.get(position));
                Log.i(TAG, "[onItemClick] position = " + position);
            } else {
                mapSelectedContact.remove(position);
            }
        } else {
            Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
            intent.putExtra(PsDatabaseHelper.ContactsColumns._ID, mListPrivateContact.get(position).getContactId());
            startActivityForResult(intent, CALL_CONTACT_DETAIL);
        }
    }

    @Override
    public void onItemLongClick(ContactListAdapter.ContactHolder viewHolder, int position) {
        isShowCheckBox = true;
        mapSelectedContact.clear(); // 清空本地map
        viewHolder.checkBox.setChecked(true); // 长按的那一项选中
        ContactListAdapter.map_allCheckBoxSelectedStatus.put(position, true);//选中长按项：更新适配器状态数据
        mapSelectedContact.put(position, mListPrivateContact.get(position));// 选中长按项：更新本地数据
        Log.i(TAG, "[onItemClick] mapSelectedContact.put-firstput position= " + position);
        mDataAdapter.showCheckBox(isShowCheckBox);
        // 给floatActionButton添加属性翻转动画
        /*ObjectAnimator.ofFloat(floatActionButton, "rotationY", 0.0F, 180.0F).setDuration(500).start();
        floatActionButton.setImageResource(R.mipmap.delete);*/
        // 第一个参数fromDegrees为动画起始时的旋转角度
        // 第二个参数toDegrees为动画旋转到的角度
        // 第三个参数pivotXType为动画在X轴相对于物件位置类型
        // 第四个参数pivotXValue为动画相对于物件的X坐标的开始位置
        // 第五个参数pivotXType为动画在Y轴相对于物件位置类型
        // 第六个参数pivotYValue为动画相对于物件的Y坐标的开始位置
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, +315.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(500);
        floatActionButton.startAnimation(rotateAnimation);
    }

    /**
     * 创建加载更多选项
     */
    private void createLoadMoreView() {
        if (mHeaderAdapter.footerViewCount() == 0) {
            //加载list尾部布局
            View loadMoreView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.recycle_view_footer, mRecyclerView, false);
            //添加一个尾部布局
            mHeaderAdapter.addFooterView(loadMoreView);
        }
    }

    @Override
    public void addData(List<ContactInfo> data) {
        Log.i(TAG, "[addData] data.size() = " + data.size());
        isOverLoad = false;
        mListPrivateContact.addAll(data);
        mDataAdapter.setData(data, isOverLoad);
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        currentOffset = 0;
        mListPrivateContact.clear();// 先清空,避免数据积累
        isOverLoad = true;
        initData();
//        isOverLoad = true;
//        mDataAdapter.setData(mListPrivateContact, isOverLoad);
        Log.i(TAG, "[onRefresh] start mListPrivateContact.size = " + mListPrivateContact.size());
        mSwipeLayout.setRefreshing(false);
    }

    /**
     * 判断有没有更多数据可提供加载
     *
     * @return
     */
    private boolean hasLoadMore() {
        return currentOffset < dataCounts;
    }

    /**
     * 上拉操作触发，加载更多数据
     *
     * @param currentPage
     */
    public void loadMore(int currentPage) {
        isOverLoad = false;
        if (hasLoadMore()) {
            initData();
            return;
        }
        mHeaderAdapter.removeFooterView();
        Toast.makeText(getContext(), getContext().getResources().getText(R.string.no_load_more), Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化数据（获取私密联系人：is_private_contacts = 1）
     */
    private void initData() {
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
            //获取分页数据
            dataCounts = mDb.getContactCounts();
            mListContactCache = mDb.getContactsByPage(pageSize, currentOffset);
            Log.i(TAG, "[onLoadMore] contacts.size = " + mListContactCache.size() + " pagesize = " + pageSize + "currentOffset" + currentOffset);
            currentOffset += pageSize;
            return null;
        }

        /**
         * 运行在ui线程中，在doInBackground()执行完毕后执行
         */
        @Override
        protected void onPostExecute(Integer integer) {
            mListPrivateContact.addAll(mListContactCache);
            mDataAdapter.setData(mListContactCache, isOverLoad);
            //如果数据不满一页，隐藏页脚加载更多item
            if (dataCounts <= pageSize) {
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

    @Override
    public void setLoader(PrivateSpaceLoader loader) {
        mLoader = loader;
    }

    @Override
    public void updateProcess(int action, int index) {
        Log.d(TAG, "[refresh] progress + 1");
        NotificationUtils.getInstance(getContext()).updateProgress(action, index);
//        onRefresh();
//        mDataAdapter.setData(mListPrivateContact);
    }
}
