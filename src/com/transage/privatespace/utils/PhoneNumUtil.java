package com.transage.privatespace.utils;

import android.util.Log;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;


/**
 * Created by yanjie.xu on 2017/10/27.
 */

public class PhoneNumUtil {
    //格式化电话号码 add by wangmeng 20170821
    public static String formatNumber(String number) {
        Log.d("PhoneNumUtil", "[PhoneNumUtil] number = " + number);
        String nationalNumber = "";
        try {
            if (number.contains("+")){
                PhoneNumberUtil instance = PhoneNumberUtil.getInstance();

                Phonenumber.PhoneNumber parse = instance.parse(number, "");
                nationalNumber = String.valueOf(parse.getNationalNumber());
            }else{
                nationalNumber = number;
            }
            Log.d("PhoneNumUtil", "[PhoneNumUtil] nationalNumber = " +  nationalNumber);
        } catch (NumberParseException e) {
            Log.d("PhoneNumUtil", "[PhoneNumUtil] NumberParseException = " + e.toString());
            e.printStackTrace();
        }
        return nationalNumber.replaceAll(" ", "").replaceAll("-", "").trim();
    }
}
