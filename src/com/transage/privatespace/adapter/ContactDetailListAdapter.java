package com.transage.privatespace.adapter;

import android.content.Context;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transage.privatespace.R;
import com.transage.privatespace.bean.CallRecord;
import com.transage.privatespace.loader.PrivateSpaceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanjie.xu on 2017/9/18.
 */

public class ContactDetailListAdapter extends RecyclerView.Adapter<ContactDetailListAdapter.CallHolder> {
    private static final String TAG = "ContactDetailListAdapter";
    private Context mContext;
    //所有联系人数据
    private List<CallRecord> mCallRecordList = new ArrayList<>();

    public ContactDetailListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public CallHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_call_detail, viewGroup, false);
        return new CallHolder(view);
    }

    @Override
    public void onBindViewHolder(CallHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);
        CallRecord call = mCallRecordList.get(position);
        //最新的通话记录是list的最后一项
        int type = call.getType();
        int imageId = -1;
        int labelId = -1;
        if (type == CallLog.Calls.INCOMING_TYPE) { //呼入
            imageId = android.R.drawable.sym_call_incoming;
            labelId = R.string.call_in_label;
        } else if (type == CallLog.Calls.OUTGOING_TYPE) { //呼出
            imageId = android.R.drawable.sym_call_outgoing;
            labelId = R.string.call_out_label;
        } else if (type == CallLog.Calls.MISSED_TYPE) { //未接
            imageId = android.R.drawable.sym_call_missed;
            labelId = R.string.call_missed_label;
        }

        if (imageId > 0){
            viewHolder.callTypeImg.setImageResource(imageId);
        }

        if (labelId > 0){
            viewHolder.callTypeStr.setText(labelId);
        }

        viewHolder.callData.setText(PrivateSpaceLoader.formartDate(call.getDate()));
        viewHolder.callTimeLong.setText(PrivateSpaceLoader.formartTime(call.getDuration()));
    }

    @Override
    public int getItemCount() {
        return mCallRecordList.size();
    }

    public class CallHolder extends RecyclerView.ViewHolder {
        public ImageView callTypeImg;
        public TextView callTypeStr, callData,callTimeLong;

        public CallHolder(View itemView) {
            super(itemView);
            callTypeImg = (ImageView) itemView.findViewById(R.id.item_call_type_img);
            callTypeStr = (TextView) itemView.findViewById(R.id.item_call_type);
            callData = (TextView) itemView.findViewById(R.id.item_call_data);
            callTimeLong = (TextView) itemView.findViewById(R.id.item_call_time_long);
        }
    }

    /**
     * 设置数据
     *
     * @param contactInfos
     * @param isOverLoad
     */
    public void setData(List<CallRecord> callRecordList) {
        mCallRecordList.addAll(callRecordList);
        notifyDataSetChanged();
    }
}
