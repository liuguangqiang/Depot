package com.liuguangqiang.depot.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.liuguangqiang.depot.Depot;

import java.util.HashMap;
import java.util.logging.Logger;

import rx.Observable;
import rx.Observer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Depot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Depot.getInstance().init(getApplicationContext());

        Depot.getInstance().put("key2", "2", 10, false);

        Log.i(TAG, "nowTimestamp:" + Depot.getInstance().nowTimestamp());

        Log.i(TAG, "" + Depot.getInstance().getExpire("key2"));
        Log.i(TAG, "key2 is expired:" + Depot.getInstance().isExpired("key2"));
        Log.i(TAG, Depot.getInstance().get("key2"));

    }
}
