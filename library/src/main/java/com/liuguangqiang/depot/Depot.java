package com.liuguangqiang.depot;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Eric on 16/7/1.
 */
public class Depot {

    private static final String TAG = "Depot";
    private static final long MAX_SIZE = 1024 * 1024 * 8;
    private static final String suffix = "_expire";

    private DepotDiskCache depotDiskCache;
    private Gson gson;
    private boolean initialised = false;

    private static class DepotLoader {
        private static final Depot INSTANCE = new Depot();
    }

    private Depot() {
    }

    public static Depot getInstance() {
        return DepotLoader.INSTANCE;
    }

    public void init(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            depotDiskCache = new DepotDiskCache(cacheDir, 1, MAX_SIZE);
            initialised = true;
            gson = new Gson();
            Log.i(TAG, "Depot initialize success");
        } catch (IOException e) {
            initialised = false;
            Log.i(TAG, "Depot initialize failure");
            e.printStackTrace();
        }
    }

    public String get(String key) {
        chkInitialised();

        if (!isExpired(key)) {
            return depotDiskCache.get(key);
        } else {
            remove(key);
            return null;
        }
    }

    public <T> T get(String key, Type type) {
        chkInitialised();

        return gson.fromJson(get(key), type);
    }

    public void put(String key, String value) {
        put(key, value, 0);
    }

    public void put(String key, Object object) {
        put(key, object, 0);
    }

    public void put(String key, Object object, long expire) {
        put(key, gson.toJson(object), expire);
    }

    public void put(String key, String value, long expire) {
        chkInitialised();
        depotDiskCache.put(key, value);

        if (expire > 0) {
            depotDiskCache.put(key + suffix, "" + (nowTimestamp() + expire));
        } else {
            depotDiskCache.put(key + suffix, "" + 0);
        }
    }

    public boolean contains(String key) {
        chkInitialised();
        return depotDiskCache.contains(key);
    }

    public void remove(String key) {
        chkInitialised();
        depotDiskCache.remove(key);
        depotDiskCache.remove(key + suffix);
    }

    public <T> Observable<T> getWithAsync(final String key, final Type type) {
        chkInitialised();
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    T t = get(key, type);
                    subscriber.onNext(t);
                    subscriber.onCompleted();
                } catch (Exception exception) {
                    subscriber.onError(exception);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> putWithAsync(final String key, final Type type) {
        return putWithAsync(key, type, 0);
    }

    public Observable<Boolean> putWithAsync(final String key, final Type type, final long expire) {
        chkInitialised();
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    put(key, type, expire);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (Exception exception) {
                    subscriber.onError(exception);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public boolean isExpired(String key) {
        long expire = getExpire(key);
        if (expire == 0 || expire > nowTimestamp()) {
            return false;
        }
        Log.i(TAG, key + " has expired or removed.");
        return true;
    }

    private long getExpire(String key) {
        if (contains(key + suffix)) {
            return Long.parseLong(depotDiskCache.get(key + suffix));
        } else {
            return -1;
        }
    }

    private long nowTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private void chkInitialised() {
        if (!initialised) {
            throw new IllegalStateException("Must initialised before use Depot.");
        }
    }

}
