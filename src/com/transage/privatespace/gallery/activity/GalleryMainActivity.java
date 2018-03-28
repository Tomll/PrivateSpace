package com.transage.privatespace.gallery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.transage.privatespace.R;
import com.transage.privatespace.activity.BaseActivity;
import com.transage.privatespace.gallery.photozoom.Folders;
import com.transage.privatespace.gallery.videozoom.PrivateVideoAlbum;
import com.transage.privatespace.gallery.videozoom.VideoAlbum;

/**
 * Created by dongrp on 2017/7/1.
 * 私密图库的主界面
 */

public class GalleryMainActivity extends BaseActivity implements View.OnClickListener {

    //private final String SDcardPath = "storage/emulated/0/";
    private final String SDcardPath = Environment.getExternalStorageDirectory().toString() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_main);

/*        //读写权限 因为android:sharedUserId="android.uid.system"，所以不需要权限请求了
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(GalleryMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(GalleryMainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }*/

        //主界面点击 私密图片或私密视频，呈现不同的按钮
        if (getIntent().getBooleanExtra("isPhoto", false)) {
            findViewById(R.id.button_priv_album).setVisibility(View.VISIBLE);
            findViewById(R.id.button_add_priv_photo).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_priv_video_album).setVisibility(View.VISIBLE);
            findViewById(R.id.button_add_priv_video).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_priv_album:
                startActivity(new Intent(GalleryMainActivity.this, PrivateAlbum.class));
                break;
            case R.id.button_add_priv_photo:
                startActivity(new Intent(GalleryMainActivity.this, Folders.class));
                break;
            case R.id.button_priv_video_album:
                startActivity(new Intent(GalleryMainActivity.this, PrivateVideoAlbum.class));
                break;
            case R.id.button_add_priv_video:
                startActivity(new Intent(GalleryMainActivity.this, VideoAlbum.class));
                break;
            default:
                break;
        }
    }


}
