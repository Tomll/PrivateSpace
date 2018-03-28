package com.transage.privatespace.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.transage.privatespace.R;

/**
 * Created by ruipan.dong on 2017/9/27.
 */

public class UseHelp extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_help);
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
