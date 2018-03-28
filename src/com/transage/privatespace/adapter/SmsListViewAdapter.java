package com.transage.privatespace.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.SmsInfo;
import com.transage.privatespace.vcard.util.Log;
import com.transage.privatespace.view.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dongrp on 2016/9/12.
 */
public class SmsListViewAdapter extends RecyclerView.Adapter<SmsListViewAdapter.SmsHolder> {
    private Context mContext;
    private List<SmsInfo> listSmslist;

    /**
     * 定义点击事件监听器
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
    private OnItemClickLitener mOnItemClickLitener;
    public SmsListViewAdapter(Context context, List<SmsInfo> listSmslist) {
        this.mContext = context;
        this.listSmslist = listSmslist;
    }


    public SmsListViewAdapter.SmsHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return  new SmsHolder(LayoutInflater.from(mContext).inflate(R.layout.item_sms, viewGroup, false));
    }

    public void onBindViewHolder(SmsListViewAdapter.SmsHolder viewHolder, int position) {
        //数据适配
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String name = listSmslist.get(position).getPerson();
        String date = simpleDateFormat.format(new Date(listSmslist.get(position).getDate()));// 收发时间
        Log.e("wangmeng","-------name--------"+name);
        Log.e("wangmeng","-------date--------"+date);
        if(name.isEmpty()){
            viewHolder.textView1.setText(listSmslist.get(position).getAddress());
        }else{
            viewHolder.textView1.setText(name);
        }
        viewHolder.textView2.setText(listSmslist.get(position).getBody());
        viewHolder.textView3.setText(date);
        //设置tag
        viewHolder.itemView.setTag(viewHolder);
        viewHolder.photo.setBorderWidth(2);
        viewHolder.photo.setBorderColor(0xffd4d4d4);
        // 如果设置了回调，则设置item点击事件
        if (mOnItemClickLitener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(viewHolder.itemView, pos);
                    return false;
                }
            });
        }
    }

    public int getItemCount() {
        return  listSmslist.size();
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder viewHolder;
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context).inflate(R.layout.item_sms,parent,false);
//            viewHolder = new ViewHolder();
//            viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
//            viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
//            viewHolder.textView3 = (TextView) convertView.findViewById(R.id.textView3);
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//        //数据适配
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        String name = listSmslist.get(position).getPerson();
//        String date = simpleDateFormat.format(new Date(listSmslist.get(position).getDate()));// 收发时间
//        Log.e("wangmeng","-------name--------"+name);
//        Log.e("wangmeng","-------date--------"+date);
//        if(name.isEmpty()){
//            viewHolder.textView1.setText(listSmslist.get(position).getAddress());
//        }else{
//            viewHolder.textView1.setText(name);
//        }
//        viewHolder.textView2.setText(listSmslist.get(position).getBody());
//        viewHolder.textView3.setText(date);
//        return convertView;
//    }
    public class SmsHolder extends RecyclerView.ViewHolder {
        public CircleImageView photo;
        public TextView textView1, textView2, textView3;


        public SmsHolder(View itemView) {
            super(itemView);
            photo = (CircleImageView) itemView.findViewById(R.id.item_sms_photo);
            textView1 = (TextView) itemView.findViewById(R.id.textView1);
            textView2 = (TextView) itemView.findViewById(R.id.textView2);
            textView3 = (TextView) itemView.findViewById(R.id.textView3);

        }
    }
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setData(List<SmsInfo> smsRecordList){
//        listSmslist.clear();
//        listSmslist.addAll(smsRecordList);
        notifyDataSetChanged();
    }
//    class ViewHolder {
//        TextView textView1, textView2, textView3;
//    }
}
