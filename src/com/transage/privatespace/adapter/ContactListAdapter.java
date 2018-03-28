package com.transage.privatespace.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.transage.privatespace.utils.CallUtils;
import com.transage.privatespace.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanjie.xu on 2017/9/18.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactHolder> {
    private static final String TAG = "ContactListAdapter";
    private Context mContext;
    //所有联系人数据
    private List<ContactInfo> mContactInfos = new ArrayList<>();
    private boolean isShowCheckBox = false;
    // 该map记录每一个item中的chengckBox的选中状态并根据状态进行设置，避免滑动的时候混乱错位
    public static HashMap<Integer, Boolean> map_allCheckBoxSelectedStatus = new HashMap<>();

    /**
     * 定义点击事件监听器
     */
    public interface OnItemClickLitener {
        void onItemClick(ContactListAdapter.ContactHolder holder, int position);

        void onItemLongClick(ContactListAdapter.ContactHolder holder, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public ContactListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ContactListAdapter.ContactHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact_list, viewGroup, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactListAdapter.ContactHolder viewHolder, int position) {
//        Object tag = viewHolder.itemView.getTag();
//        if (tag != null || (int)tag != position){
////            viewHolder = new CallHolder(viewHolder.itemView);
//        }

        viewHolder.itemView.setTag(position);
        // 数据适配
        if (isShowCheckBox) {
            setScaleAni(viewHolder.checkBox, 0.0f, 1.0f, 200);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        } else {
            // setScaleAni(viewHolder.checkBox,1.0f,0.0f,200);
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        ContactInfo contactInfo = mContactInfos.get(position);

        Log.i(TAG, "map_allCheckBoxSelectedStatus .size = " + map_allCheckBoxSelectedStatus.size());
        viewHolder.checkBox.setChecked(map_allCheckBoxSelectedStatus.get(position));
        viewHolder.tvName.setText(contactInfo.getDisplayName());
        if (contactInfo.getHasPhoneNumber() > 0 && contactInfo.getPhones().size() > 0) {
            viewHolder.tvPhoneNum.setText(contactInfo.getPhones().get(0).phoneNumber);
        }

        if (contactInfo.getPhoto() != null) {
            viewHolder.photo.setImageBitmap(contactInfo.getPhoto());
        } else {
            viewHolder.photo.setImageResource(R.drawable.ic_person_black_128dp);
        }

        viewHolder.photo.setBorderWidth(2);
        viewHolder.photo.setBorderColor(0xffd4d4d4);

        viewHolder.phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallUtils.callPhoneOrSendSms(mContext, 1, contactInfo.getPhones().get(0).phoneNumber);// 打电话
            }
        });
        viewHolder.smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallUtils.callPhoneOrSendSms(mContext, 2, contactInfo.getPhones().get(0).phoneNumber);// 发短信
            }
        });

        // 如果设置了回调，则设置item点击事件
        if (mOnItemClickLitener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(viewHolder, pos);
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(viewHolder, pos);
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mContactInfos.size();
    }

    /**
     * 方法：显示 与 隐藏checkBox
     */
    public void showCheckBox(boolean isShowCheckBox) {
        this.isShowCheckBox = isShowCheckBox;
        notifyDataSetChanged();
    }

    /**
     * 为控件添加缩放动画
     */
    public static void setScaleAni(View V, float fromScale, float toScale, long ANITIME) {
        AnimationSet aniSet = new AnimationSet(true);
        // final int ANITIME = 500;
        // 尺寸变化动画，设置尺寸变化
        ScaleAnimation scaleAni = new ScaleAnimation(fromScale, toScale, fromScale, toScale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAni.setDuration(ANITIME); // 设置动画效果时间
        aniSet.addAnimation(scaleAni); // 将动画效果添加到动画集中
        V.startAnimation(aniSet); // 添加光效动画到控件
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView tvName, tvPhoneNum;
        public ImageButton phoneButton, smsButton;
        public CircleImageView photo;

        public ContactHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.item_contact_list_cb);
            photo = (CircleImageView) itemView.findViewById(R.id.item_contact_list_photo);
            tvName = (TextView) itemView.findViewById(R.id.item_contact_list_name);
            tvPhoneNum = (TextView) itemView.findViewById(R.id.item_contact_list_number);
            phoneButton = (ImageButton) itemView.findViewById(R.id.item_contact_list_button_call);
            smsButton = (ImageButton) itemView.findViewById(R.id.item_contact_list_button_sms);
        }
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    /**
     * 设置数据
     *
     * @param contactInfos
     * @param isOverLoad
     */
    public void setData(List<ContactInfo> contactInfos, boolean isOverLoad) {
        if (isOverLoad) {
            mContactInfos.clear();
        }
        Log.i(TAG, "[addData] mContactInfos.size() = " + mContactInfos.size() + " data.size = " + contactInfos.size());
        mContactInfos.addAll(contactInfos);
        Log.i(TAG, "[addData] mContactInfos.size() = " + mContactInfos.size());
        initMapCheckStatus(contactInfos, isOverLoad);
        notifyDataSetChanged();
    }

    /**
     * 初始化 map_allCheckBoxSelectedStatus
     */
    public static void initMapCheckStatus(List<ContactInfo> contactInfos, boolean isOverLoad) {
        // 初始化map_allCheckBoxSelectedStatus，默认value = false
        if (isOverLoad) {
            map_allCheckBoxSelectedStatus.clear();
        }
        // 初始化map_allCheckBoxSelectedStatus，默认value = false
        int oldMapCounts = map_allCheckBoxSelectedStatus.size();
        for (int i = 0; i < contactInfos.size(); i++) {
            map_allCheckBoxSelectedStatus.put(oldMapCounts + i, false);
        }
    }
}
