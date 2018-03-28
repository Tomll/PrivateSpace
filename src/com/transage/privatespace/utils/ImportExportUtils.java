package com.transage.privatespace.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.transage.privatespace.activity.Login;
import com.transage.privatespace.bean.People;
import com.transage.privatespace.vcard.pim.VDataBuilder;
import com.transage.privatespace.vcard.pim.VNode;
import com.transage.privatespace.vcard.pim.vcard.ContactStruct;
import com.transage.privatespace.vcard.pim.vcard.VCardComposer;
import com.transage.privatespace.vcard.pim.vcard.VCardException;
import com.transage.privatespace.vcard.pim.vcard.VCardParser;
import com.transage.privatespace.vcard.provider.Contacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanjie.xu on 2017/7/19.
 */

public class ImportExportUtils {
    private static final String TAG = "ImportExportUtils";

    //public static final String SHARE_SAVEVCF = "savevcf";
    public static final String KEY_SHARE_SAVEVCF = "isSaveVcf";


    /**
     * 是否保存数据到vcf文件
     */
//    public static boolean isVcf = true;
    //run the WriteExample first or provide your own "example.vcard"

    public static List<People> readData(String vcf_path) {
        List<People> peopleList = new ArrayList<>();
        VCardParser parser = new VCardParser();
        VDataBuilder builder = new VDataBuilder();
        //read whole file to string
        Log.i(TAG, "ImportExportUtils.readDate()");
        String file = Environment.getExternalStorageDirectory() + vcf_path;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            String vcardString = "";
            String line;
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            //parse the string
            boolean parsed = parser.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + vcf_path);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //get all parsed contacts
        List<VNode> pimContacts = builder.vNodeList;

        //do something for all the contacts
        for (VNode contact : pimContacts) {
            ContactStruct contactStruct = ContactStruct.constructContactFromVNode(contact, 1);
            android.util.Log.i(TAG, "Found contact: " + contactStruct.name);
            People people = new People();
            List<ContactStruct.PhoneData> phoneDataList = contactStruct.phoneList;
            for (ContactStruct.PhoneData phoneData : phoneDataList) {
                people.setPhoneNum(phoneData.data);
            }
            people.setDisplayName(contactStruct.name);
            peopleList.add(people);
            //similarly for other properties (N, ORG, TEL, etc)
            //...
        }
        return peopleList;
    }

    public static void writeData(List<People> peoples, String vcf_path) {
        OutputStreamWriter writer;
        File file = new File(Environment.getExternalStorageDirectory(), vcf_path);
        try{
            //得到存储卡的根路径，将example.vcf写入到根目录下
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            //create a contact
            VCardComposer composer = new VCardComposer();
            for (People people : peoples) {
                ContactStruct contact = new ContactStruct();
                contact.name = people.getDisplayName();
                contact.addPhone(Contacts.Phones.TYPE_MOBILE, people.getPhoneNum(), null, true);
                String vcardString = composer.createVCard(contact, VCardComposer.VERSION_VCARD30_INT);
                android.util.Log.i(TAG, "writeData: vcardString = " + vcardString);
                writer.write(vcardString);
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void removeDataFile(String vcfFilePath) {
        File file = new File(Environment.getExternalStorageDirectory(), vcfFilePath);
        if (file.exists()){
            file.delete();
        }
    }

    public static boolean isVcf(Context context){
        SharedPreferences sp = context.getSharedPreferences(Login.PRIVATE_SPACE_SP, Context.MODE_PRIVATE);
        boolean isSaveVcf = sp.getBoolean(KEY_SHARE_SAVEVCF, false);
        Log.i(TAG, "[isVcf] isSaveVcf = " + isSaveVcf);
        return isSaveVcf;
    }

//    try{
////                    ImportExportUtils.writeData();
//        List<ContactInfo> contactInfo = ContactInfo.ContactHandler.getInstance().getContactInfo(PrivateContactsActivity.this);
//        Log.i(TAG, "CatactInfo" + contactInfo.toArray().toString());
//        ContactInfo.ContactHandler.getInstance().backupContacts(PrivateContactsActivity.this, contactInfo);
//        Toast.makeText(PrivateContactsActivity.this, "备份联系人", Toast.LENGTH_SHORT).show();
//    }catch (Exception e){
//        e.printStackTrace();
//    }

//    try{
////                    ImportExportUtils.readData();
//        List<ContactInfo> contactInfos = ContactInfo.ContactHandler.getInstance().restoreContacts();
//        String s = contactInfos.toArray().toString();
//        textView.setText(s);
//        Toast.makeText(PrivateContactsActivity.this, "还原备份的联系人", Toast.LENGTH_SHORT).show();
//    }catch (Exception e){
//        e.printStackTrace();
//    }
}
