package com.transage.privatespace.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanjie.xu on 2017/9/18.
 */

public class AddContactListAdapter extends RecyclerView.Adapter<AddContactListAdapter.AddHolder> {
    private static final String TAG = "AddContactListAdapter";
    private Context mContext;
    //所有联系人数据
    private List<ContactInfo> mContactList = new ArrayList<>();
    private boolean isShowCheckBox = false;
    // 该map记录每一个item中的chengckBox的选中状态并根据状态进行设置，避免滑动的时候混乱错位
    public static HashMap<Integer, Boolean> map_allCheckBoxSelectedStatus = new HashMap<Integer, Boolean>();

    /**
     * 定义点击事件监听器
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public AddContactListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public AddContactListAdapter.AddHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        return new AddHolder(LayoutInflater.from(mContext).inflate(R.layout.item_add_contact, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(AddContactListAdapter.AddHolder viewHolder, int position) {
        // 数据适配
        Bitmap photo = mContactList.get(position).getPhoto();
        //如果头像为空，则加载默认头像
        if (photo == null) {
            viewHolder.photo.setImageResource(R.drawable.ic_person_black_128dp);
        }else{
            viewHolder.photo.setImageBitmap(photo);
        }
        viewHolder.checkBox.setChecked(map_allCheckBoxSelectedStatus.get(position));
        viewHolder.name.setText(mContactList.get(position).getDisplayName());
        Log.i(TAG, mContactList.get(position).toString());
        if (mContactList.get(position).getHasPhoneNumber() > 0 && mContactList.get(position).getPhones().size() > 0) {
            viewHolder.phoneNum.setText(mContactList.get(position).getPhones().get(0).phoneNumber);
        }

        //设置tag
        viewHolder.itemView.setTag(viewHolder);

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

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public class AddHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView name, phoneNum;
        public CircleImageView photo;

        public AddHolder(View itemView) {
            super(itemView);
            photo = (CircleImageView) itemView.findViewById(R.id.item_add_contact_photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.item_add_contact_checkbox);
            name = (TextView) itemView.findViewById(R.id.item_add_contact_name);
            phoneNum = (TextView) itemView.findViewById(R.id.item_add_contact_phone);
        }
    }

    //设置点击事件监听器
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    //设置并刷新数据
    public void setData(List<ContactInfo> contactInfos, boolean isOverLoad){
        if (isOverLoad){
            mContactList.clear();
        }
        mContactList.addAll(contactInfos);
        initMapCheckStatus(contactInfos, isOverLoad);
        Log.i(TAG, "[setData] mContactList.size = " + mContactList.size());
        notifyDataSetChanged();
    }

    /**
     * 初始化 map_allCheckBoxSelectedStatus
     */
    public static void initMapCheckStatus(List<ContactInfo> contactInfos, boolean isOverLoad) {
        // 初始化map_allCheckBoxSelectedStatus，默认value = false
        if (isOverLoad){
            map_allCheckBoxSelectedStatus.clear();
        }
        // 初始化map_allCheckBoxSelectedStatus，默认value = false
        int oldMapCounts = map_allCheckBoxSelectedStatus.size();
        for (int i = 0; i < contactInfos.size(); i++) {
            map_allCheckBoxSelectedStatus.put(oldMapCounts + i, false);
        }
    }
}

