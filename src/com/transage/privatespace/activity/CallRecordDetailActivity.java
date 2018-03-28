package com.transage.privatespace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.adapter.ContactDetailListAdapter;
import com.transage.privatespace.adapter.ContactListAdapter;
import com.transage.privatespace.bean.CallRecord;
import com.transage.privatespace.bean.CallRecordInfo;
import com.transage.privatespace.database.PsDatabaseHelper;
import com.transage.privatespace.utils.CallUtils;
import com.transage.privatespace.view.CircleImageView;

import java.util.List;

/**
 * Created by yanjie.xu on 2017/9/28.
 */

public class CallRecordDetailActivity extends AppCompatActivity {
    private static final String TAG = "CallRecordDetailActivity";

    public static final String DELETE_CALL = "action.delete.call";
    private CallRecordInfo mCallRecordInfo;
    private List<CallRecord> mCallRecordList;
    private CircleImageView mPhoto;
    private ImageView mCallAction;
    private TextView mName, mNum;
    private RecyclerView mRecyclerView;
    private Toolbar mToolBar;
    private ContactDetailListAdapter mDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_details);
        mCallRecordInfo = (CallRecordInfo) getIntent().getSerializableExtra("call_record_info");
        initView();
        initData();
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

    private void initView() {
        mPhoto = (CircleImageView) findViewById(R.id.call_detail_photo);
        mName = (TextView) findViewById(R.id.call_detail_name);
        mNum = (TextView) findViewById(R.id.call_detail_num);
        mCallAction = (ImageView) findViewById(R.id.call_detail_action_call);
        mRecyclerView = (RecyclerView) findViewById(R.id.call_detail_list);
        mToolBar = (Toolbar) findViewById(R.id.call_detail_toolbar);

        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        //创建适配器
        mDataAdapter = new ContactDetailListAdapter(this);
        //设置adapter
        mRecyclerView.setAdapter(mDataAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
                        AlertDialog dialog = new AlertDialog.Builder(CallRecordDetailActivity.this)
                                .setTitle(getString(R.string.delete_call_record))
                                .setMessage(getString(R.string.confirm_delete_call_record))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        Toast.makeText(CallRecordDetailActivity.this, "action_delete",
//                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent();
                                        intent.setAction(DELETE_CALL);
                                        intent.putExtra(PsDatabaseHelper.CallRecordClumns.NUMBER, mCallRecordInfo.phone());
                                        setResult(RESULT_OK, intent);
                                        CallRecordDetailActivity.this.finish();
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
                CallRecordDetailActivity.this.finish();
            }
        });

        Bitmap image = mCallRecordInfo.photo();
        if (image != null) {
            mPhoto.setImageBitmap(image);
        }

        mName.setText(mCallRecordInfo.name());
        mNum.setText(mCallRecordInfo.phone());

        mCallAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击通话记录，拨打电话
                CallUtils.callPhoneOrSendSms(CallRecordDetailActivity.this, 1, mCallRecordInfo.phone());
            }
        });
    }

    private void initData() {
        if (mCallRecordInfo == null) {
            return;
        }
        mCallRecordList = mCallRecordInfo.getCallRecordList();
        mDataAdapter.setData(mCallRecordList);
    }
}
