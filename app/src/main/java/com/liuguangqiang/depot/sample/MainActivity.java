package com.liuguangqiang.depot.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.liuguangqiang.depot.Depot;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Depot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Depot.getInstance().init(getApplicationContext());

        if (Depot.getInstance().contains("hashMap")) {
            Log.i(TAG, "init from disk");
        } else {
            Log.i(TAG, "init from new object");
        }

        Depot.getInstance().put("key", "abc");

        HashMap<String, String> hashMap = new HashMap<>();
        Depot.getInstance().put("hashMap", hashMap);

        Log.i(TAG, Depot.getInstance().get("key"));

        if (!Depot.getInstance().isExpired("key1")) {
            Log.i(TAG, Depot.getInstance().get("key1"));
        }
    }

}
