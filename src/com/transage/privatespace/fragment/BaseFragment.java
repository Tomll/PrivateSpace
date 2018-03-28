package com.transage.privatespace.fragment;

import android.support.v4.app.Fragment;

import com.transage.privatespace.bean.ContactInfo;
import com.transage.privatespace.loader.PrivateSpaceLoader;

import java.util.List;

/**
 * Created by yanjie.xu on 2017/7/21.
 */

public abstract class BaseFragment extends Fragment{
    public static PrivateSpaceLoader mLoader;

    public abstract void setLoader(PrivateSpaceLoader loader);

    public abstract void updateProcess(int action, int index);

    public void addData(List<ContactInfo> data){

    };
}
