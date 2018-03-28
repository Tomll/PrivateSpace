package com.transage.privatespace.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.transage.privatespace.R;
import com.transage.privatespace.adapter.AddSmsListViewAdapter;
import com.transage.privatespace.bean.SmsInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.vcard.util.Log;

import java.util.ArrayList;

/**
 * Created by meng.wang on 2017/9/2.
 */

public class PrivateSms extends BaseActivity {
    private View view;
    private ArrayList<SmsInfo> listSmslist = new ArrayList<>();
    private AddSmsListViewAdapter addSmsListViewAdapter;
    private String mAddress;
    private String mPerson;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏TitleBar
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
        setContentView(R.layout.activity_add_sms);


    }

    @Override
    public void onResume() {
        super.onResume();
        // 检查并申请READ_SMS权限
        if (ContextCompat.checkSelfPermission(PrivateSms.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PrivateSms.this, new String[]{Manifest.permission.READ_SMS}, 1);
            return;
        }
        initData();
        initViewAndAdapter();
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
        ListView listView = (ListView)findViewById(R.id.list_sms_recv);
        addSmsListViewAdapter = new AddSmsListViewAdapter(PrivateSms.this, listSmslist);
        listView.setAdapter(addSmsListViewAdapter);
        addSmsListViewAdapter.notifyDataSetChanged();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrivateSms.this);
                builder.setMessage(getString(R.string.confirm_delete));
                builder.setTitle(getString(R.string.hint));
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String id = String.valueOf(listSmslist.get(position).getId());
                        if(listSmslist.remove(position)!=null){
                            new DatabaseAdapter(PrivateSms.this).deleteSmsById(id);
                            Log.e("wangmeng","success");
                            Toast.makeText(PrivateSms.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        }else {
                            Log.e("wangmeng","failed");
                            Toast.makeText(PrivateSms.this, getString(R.string.delete_fail), Toast.LENGTH_SHORT).show();
                        }
                        addSmsListViewAdapter.notifyDataSetChanged();
                    }
                });
//
//
                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
                return false;
            }
        });
    }

    /**
     * 初始化数据
     */
    public void initData() {
        Intent intent =getIntent();
        mAddress=intent.getStringExtra("address");
        mPerson = intent.getStringExtra("person");
        Log.e("wangmeng","----mAddress---"+mAddress);
        TextView tv = (TextView) findViewById(R.id.sms_tv);
        ImageView imageView = (ImageView) findViewById(R.id.sms_back);
        FloatingActionButton floatingActionButtonButton = (FloatingActionButton)findViewById(R.id.sms_float_button);
        floatingActionButtonButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //检查并申请SEND_SMS权限
                if (ActivityCompat.checkSelfPermission(PrivateSms.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PrivateSms.this, new String[]{Manifest.permission.SEND_SMS}, 1);// 申请发短信权限
                    return;
                }
                Intent  intent = new Intent(android.content.Intent.ACTION_SENDTO, Uri.parse("smsto:" + mAddress));
                startActivity(intent);
            }
        });
        if(mPerson==null){
            tv.setText(mAddress);
        }else{
            tv.setText(mPerson);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PrivateSms.this.finish();
            }
        });
        listSmslist.clear();
        listSmslist.addAll(new DatabaseAdapter(PrivateSms.this).getSmsByNum(mAddress));
    }

    

}
