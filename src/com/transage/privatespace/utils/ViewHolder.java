package com.transage.privatespace.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * ViewHolder工具类：避免适配器过多，导致写过多的ViewHolder类
 *
 * @author dongrp
 */
public class ViewHolder {
    /**
     * @param convertView ：将适配中的getView()方法中的convertView参数传递进来
     * @param id          ：要find到的childView的id
     * @return childView ：将find到的view返回
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getChildView(View convertView, int id) {
        SparseArray<View> sparseArray = (SparseArray<View>) convertView.getTag();
        if (sparseArray == null) {
            sparseArray = new SparseArray<View>();
            convertView.setTag(sparseArray);
        }
        View childView = sparseArray.get(id);
        if (childView == null) {
            childView = convertView.findViewById(id);
            sparseArray.put(id, childView);
        }
        return (T) childView;
    }
}
