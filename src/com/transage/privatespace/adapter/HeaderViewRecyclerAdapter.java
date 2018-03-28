package com.transage.privatespace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义一个适配器，为recycleview添加，列表头部和尾部Item
 * Created by yanjie.xu on 2017/9/20.
 */
public class HeaderViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "HeaderViewRecyclerAdapter";

    private static final int HEADERS_START = Integer.MIN_VALUE;
    private static final int FOOTERS_START = Integer.MIN_VALUE + 10;
    private static final int ITEMS_START = Integer.MIN_VALUE + 20;
    private static final int ADAPTER_MAX_TYPES = 100;

    private RecyclerView.Adapter mWrappedAdapter;
    private List<View> mHeaderViews, mFooterViews;
    private Map<Class, Integer> mItemTypesOffset;

    /**
     * 构造一个新的标题视图回收器适配器
     *
     * @param 适配器要包装的底层适配器
     */
    public HeaderViewRecyclerAdapter(RecyclerView.Adapter adapter) {
        mHeaderViews = new ArrayList<View>();
        mFooterViews = new ArrayList<View>();
        mItemTypesOffset = new HashMap<Class, Integer>();
        setWrappedAdapter(adapter);
    }

    /**
     * 替换底层适配器，通知RecyclerView更改
     *
     * @param 新的适配器包装
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mWrappedAdapter != null && mWrappedAdapter.getItemCount() > 0) {
            notifyItemRangeRemoved(getHeaderCount(), mWrappedAdapter.getItemCount());
        }
        setWrappedAdapter(adapter);
        notifyItemRangeInserted(getHeaderCount(), mWrappedAdapter.getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        int hCount = getHeaderCount();
        if (position < hCount) return HEADERS_START + position;
        else {
            int itemCount = mWrappedAdapter.getItemCount();
            if (position < hCount + itemCount) {
                return getAdapterTypeOffset() + mWrappedAdapter.getItemViewType(position - hCount);
            } else return FOOTERS_START + position - hCount - itemCount;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType < HEADERS_START + getHeaderCount())
            return new StaticViewHolder(mHeaderViews.get(viewType - HEADERS_START));
        else if (viewType < FOOTERS_START + getFooterCount())
            return new StaticViewHolder(mFooterViews.get(viewType - FOOTERS_START));
        else {
            return mWrappedAdapter.onCreateViewHolder(viewGroup, viewType - getAdapterTypeOffset());
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int hCount = getHeaderCount();
        if (position >= hCount && position < hCount + mWrappedAdapter.getItemCount())
            mWrappedAdapter.onBindViewHolder(viewHolder, position - hCount);
    }

    /**
     * 添加一个静态视图，显示在RecyclerView的开头。 标题按照添加的顺序显示。
     *
     * @param view要添加的标题视图
     */
    public void addHeaderView(View view) {
        mHeaderViews.add(view);
    }

    /**
     * 添加静态视图以显示在RecyclerView的末尾。 页脚按照添加的顺序显示。
     *
     * @param view要添加的页脚视图
     */
    public void addFooterView(View view) {
        mFooterViews.add(view);
    }

    @Override
    public int getItemCount() {
        return getHeaderCount() + getFooterCount() + getWrappedItemCount();
    }

    /**
     * @return 底层适配器中的项目计数
     */
    public int getWrappedItemCount() {
        return mWrappedAdapter.getItemCount();
    }

    /**
     * @return 添加标题视图的数量
     */
    public int getHeaderCount() {
        return mHeaderViews.size();
    }

    /**
     * @return 添加了页脚视图的数量
     */
    public int getFooterCount() {
        return mFooterViews.size();
    }

    private void setWrappedAdapter(RecyclerView.Adapter adapter) {
        if (mWrappedAdapter != null) mWrappedAdapter.unregisterAdapterDataObserver(mDataObserver);
        mWrappedAdapter = adapter;
        Class adapterClass = mWrappedAdapter.getClass();
        if (!mItemTypesOffset.containsKey(adapterClass)) putAdapterTypeOffset(adapterClass);
        mWrappedAdapter.registerAdapterDataObserver(mDataObserver);
    }

    private void putAdapterTypeOffset(Class adapterClass) {
        mItemTypesOffset.put(adapterClass, ITEMS_START + mItemTypesOffset.size() * ADAPTER_MAX_TYPES);
    }

    private int getAdapterTypeOffset() {
        return mItemTypesOffset.get(mWrappedAdapter.getClass());
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            int hCount = getHeaderCount();
            // TODO: No notifyItemRangeMoved method?
            notifyItemRangeChanged(fromPosition + hCount, toPosition + hCount + itemCount);
        }
    };

    /**
     * 移除页脚加载选项
     */
    public void removeFooterView(){
        mFooterViews.clear();
        notifyDataSetChanged();
    }

    /**
     * 获取footer总数
     * @return
     */
    public int footerViewCount(){
        return mFooterViews.size();
    }

    private static class StaticViewHolder extends RecyclerView.ViewHolder {
        public StaticViewHolder(View itemView) {
            super(itemView);
        }
    }
}
