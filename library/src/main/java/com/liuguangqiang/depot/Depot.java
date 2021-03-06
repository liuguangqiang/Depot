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
            Log.e(TAG, key + "has expired or removed.");
            remove(key);
            return null;
        }
    }

    public <T> T get(String key, Type type) {
        chkInitialised();

        if (!isExpired(key)) {
            return gson.fromJson(get(key), type);
        } else {
            Log.e(TAG, key + "has expired or removed.");
            remove(key);
            return null;
        }
    }

    public void put(String key, String value) {
        put(key, value, 0, true);
    }

    public void put(String key, Object object) {
        put(key, object, 0, true);
    }

    public void put(String key, Object object, long expire) {
        put(key, object, expire, true);
    }

    public void put(String key, Object object, long expire, boolean updateExpire) {
        put(key, gson.toJson(object), expire, updateExpire);
    }

    /**
     * @param key
     * @param value
     * @param expire       过期时间
     * @param updateExpire 是否需要更新缓存的过期时间
     */
    public void put(String key, String value, long expire, boolean updateExpire) {
        chkInitialised();
        depotDiskCache.put(key, value);

        if (expire > 0 && (updateExpire || !contains(key + suffix))) {
            //需要更新，或者没有存入过期时间，才保存一个新的过期时间
            Log.i(TAG, "update expire time");
            depotDiskCache.put(key + suffix, "" + (nowTimestamp() + expire));
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

    public Observable<String> getWithAsync(final String key) {
        chkInitialised();
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String result = get(key);
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (Exception exception) {
                    subscriber.onError(exception);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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

    public Observable<Boolean> putWithAsync(final String key, final Object object) {
        return putWithAsync(key, object, 0, true);
    }

    public Observable<Boolean> putWithAsync(final String key, final Object object, final long expire, final boolean updateExpire) {
        chkInitialised();
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    put(key, object, expire, updateExpire);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (Exception exception) {
                    subscriber.onError(exception);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 是否过期
     *
     * @param key
     * @return
     */
    public boolean isExpired(String key) {
        return getExpire(key) < nowTimestamp();
    }

    public long getExpire(String key) {
        if (contains(key + suffix)) {
            return Long.parseLong(depotDiskCache.get(key + suffix));
        } else {
            return 0;
        }
    }

    public long nowTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private void chkInitialised() {
        if (!initialised) {
            throw new IllegalStateException("Must initialised before use Depot.");
        }
    }

}
