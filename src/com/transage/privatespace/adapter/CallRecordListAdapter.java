package com.transage.privatespace.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.CallRecordInfo;
import com.transage.privatespace.database.DatabaseAdapter;
import com.transage.privatespace.loader.PrivateSpaceLoader;
import com.transage.privatespace.utils.CallUtils;
import com.transage.privatespace.view.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanjie.xu on 2017/9/18.
 */

public class CallRecordListAdapter extends RecyclerView.Adapter<CallRecordListAdapter.CallHolder> {
    private Context mContext;
    private List<CallRecordInfo> mCallRecords = new ArrayList<>();
    private DatabaseAdapter mDb;

    /**
     * 定义点击事件监听器
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public CallRecordListAdapter(Context context) {
        this.mContext = context;
        if (mDb == null){
            mDb = new DatabaseAdapter(mContext);
        }
    }

    @Override
    public CallRecordListAdapter.CallHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        return new CallHolder(LayoutInflater.from(mContext).inflate(R.layout.item_call_record, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(CallRecordListAdapter.CallHolder viewHolder, int position) {
        CallRecordInfo info = mCallRecords.get(position);

        Bitmap photoImg = info.photo();
        if (photoImg == null){
            viewHolder.photo.setImageResource(R.drawable.ic_person_black_128dp);
        }else {
            viewHolder.photo.setImageBitmap(photoImg);
        }

        viewHolder.photo.setBorderWidth(2);
        viewHolder.photo.setBorderColor(0xffd4d4d4);

        viewHolder.name.setText(info.name());

        //最新的通话记录是list的最后一项
        int type = mCallRecords.get(position).type();
        int imageId = -1;
        if (type == CallLog.Calls.INCOMING_TYPE) { //呼入
            imageId = R.mipmap.dial_in;
        } else if (type == CallLog.Calls.OUTGOING_TYPE) { //呼出
            imageId = R.mipmap.dial_out;
        } else if (type == CallLog.Calls.MISSED_TYPE) { //未接
            imageId = R.mipmap.missed_call;
        }

        if (imageId > 0){
            viewHolder.type.setImageResource(imageId);
        }
        //设置通话次数+时间
        viewHolder.callInfo.setText(" (" + info.counts() + ") " +
                PrivateSpaceLoader.formartDate(info.date()));

        //设置tag
        viewHolder.itemView.setTag(viewHolder);

        viewHolder.callAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击通话记录，拨打电话
                CallUtils.callPhoneOrSendSms(mContext, 1, info.phone());
            }
        });

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
        return mCallRecords.size();
    }

    public class CallHolder extends RecyclerView.ViewHolder {
        public CircleImageView photo;
        public TextView name, callInfo;
        public ImageView type, callAction;

        public CallHolder(View itemView) {
            super(itemView);
            photo = (CircleImageView) itemView.findViewById(R.id.item_call_photo);
            name = (TextView) itemView.findViewById(R.id.item_call_name);
            type = (ImageView) itemView.findViewById(R.id.item_call_type);
            callInfo = (TextView) itemView.findViewById(R.id.item_call_info);
            callAction = (ImageView) itemView.findViewById(R.id.item_call_action_call);
        }
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setData(List<CallRecordInfo> callRecordList){
        mCallRecords.clear();
        mCallRecords.addAll(callRecordList);
        notifyDataSetChanged();
    }
}
