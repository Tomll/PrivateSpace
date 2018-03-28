package com.transage.privatespace.loader;

/**
 * Created by yanjie.xu on 2017/7/20.
 */

public interface OnLoadListener {
    //指向加载的fragment对象
    enum LoadTag{
        CALLRECORD(0), CONTACT(1), SMS(2);

        private int mValue;

        LoadTag(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    public void onLoadComplete(LoadTag loadTag, int action, int index);
}
