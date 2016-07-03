package com.liuguangqiang.depot.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.liuguangqiang.depot.Depot;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Depot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Depot.getInstance().init(getApplicationContext());
        Depot.getInstance().put("key", "abc");

        Log.i(TAG, Depot.getInstance().get("key"));

        if (!Depot.getInstance().isExpired("key1")) {
            Log.i(TAG, Depot.getInstance().get("key1"));
        }
    }

}
