package com.packy1990.imagepicker;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("TAG", "onDestroy: " );
    }
}
