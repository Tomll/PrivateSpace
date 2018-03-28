package com.transage.privatespace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.SmsInfo;
import com.transage.privatespace.vcard.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by meng.wang on 2017/9/2.
 */

public class AddSmsListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SmsInfo> listSmslist;
    final int TYPE_RECV = 0;
    final int TYPE_SEND = 1;
    public AddSmsListViewAdapter(Context context, ArrayList<SmsInfo> listSmslist) {
        this.context = context;
        this.listSmslist = listSmslist;
    }

    @Override
    public int getCount() {
        return listSmslist.size();
    }

    @Override
    public Object getItem(int position) {
        return listSmslist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int type = listSmslist.get(position).getType();
        Log.e("wangmeng","----type54---------"+type);
        if(type == 1){
            return TYPE_RECV;
        }else {
            return TYPE_SEND;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        ViewHolderSend viewHolderSend = null;
        int type = getItemViewType(position);
        Log.e("wangmeng","-------72----------"+type);
//        int type = listSmslist.get(position).getType();
        if(convertView == null || viewHolder == null || viewHolderSend == null){
            switch (type){
                case TYPE_RECV:
//                    Log.e("wangmeng","-----77------");
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_all_sms_recv, parent, false);
                    viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1_recv);
                    viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2_recv);
                    convertView.setTag(R.id.tag_recv,viewHolder);
                    break;
                case TYPE_SEND:
//                    Log.e("wangmeng","-----85------");
                    viewHolderSend = new ViewHolderSend();
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_all_sms_send, parent, false);
                    viewHolderSend.imageView1 = (ImageView) convertView.findViewById(R.id.send_ok);
                    viewHolderSend.textView3 = (TextView) convertView.findViewById(R.id.textView1_send);
                    viewHolderSend.textView4 = (TextView) convertView.findViewById(R.id.textView2_send);
                    convertView.setTag(R.id.tag_send,viewHolderSend);
                    break;
                default:
                    Log.e("wangmeng","-----default------"+type);
                    break;
            }
        }else{
            switch (type){
                case TYPE_RECV:
//                    Log.e("wangmeng","-----100------");
                    viewHolder = (ViewHolder) convertView.getTag(R.id.tag_recv);
                    break;
                case TYPE_SEND:
//                    Log.e("wangmeng","-----104------");
                    viewHolderSend = (ViewHolderSend) convertView.getTag(R.id.tag_send);
                    break;
            }
        }
//        if (convertView == null) {
//            viewHolder = new ViewHolder();
//            if(type==1) {
//                convertView = LayoutInflater.from(context).inflate(R.layout.item_all_sms_recv, parent, false);
//                viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1_recv);
//                viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2_recv);
//            }else{
//                convertView = LayoutInflater.from(context).inflate(R.layout.item_all_sms_send, parent, false);
//                viewHolder.imageView1 = (ImageView) convertView.findViewById(R.id.send_ok);
//                viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1_send);
//                viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2_send);
//            }
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
        //数据适配
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        String date1 = simpleDateFormat1.format(new Date(listSmslist.get(position).getDate()));
        String date2 = simpleDateFormat2.format(new Date(listSmslist.get(position).getDate()));
        String date = simpleDateFormat.format(new Date(listSmslist.get(position).getDate()));
        //获取系统年份
        String year = simpleDateFormat1.format(new Date());
//        Log.e("wangmeng","------130-----"+year);
//        Log.e("wangmeng","------131-----"+date);
//        Log.e("wangmeng","------132-----"+date1);
//        Log.e("wangmeng","------133-----"+date2);
        switch (type){
            case TYPE_RECV:
//                Log.e("wangmeng","------136TYPE_RECV-----");
                if(year.equals(date1)){
//                    Log.e("wangmeng","------138TYPE_RECV-----");
                    viewHolder.textView2.setText(date);
                }else{
//                    Log.e("wangmeng","------141TYPE_RECV-----");
                    viewHolder.textView2.setText(date2);
                }
                viewHolder.textView1.setText(listSmslist.get(position).getBody());
                break;
            case TYPE_SEND:
//                Log.e("wangmeng","------147TYPE_SEND-----");
                if(year.equals(date1)){
//                    Log.e("wangmeng","------149TYPE_SEND-----");
                    viewHolderSend.textView4.setText(date);
                }else{
//                    Log.e("wangmeng","------152TYPE_SEND-----");
                    viewHolderSend.textView4.setText(date2);
                }
                viewHolderSend.textView3.setText(listSmslist.get(position).getBody());
                break;
        }

//        if(year.equals(date1)){
//            viewHolder.textView2.setText(date);
//        }else{
//            viewHolder.textView2.setText(date2);
//        }
//        viewHolder.textView1.setText(listSmslist.get(position).getBody());
        return convertView;
    }

    public class ViewHolder {
        TextView textView1, textView2;
    }
    public class ViewHolderSend {
        TextView textView3, textView4;
        ImageView imageView1;
    }
}