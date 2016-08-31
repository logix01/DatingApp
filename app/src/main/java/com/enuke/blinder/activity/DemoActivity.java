package com.enuke.blinder.activity;

import android.os.Bundle;

import com.enuke.blinder.R;
import com.xabber.android.ui.helper.ManagedActivity;

/**
 * Created by nitesh on 2/17/15.
 */
public class DemoActivity extends ManagedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
}
