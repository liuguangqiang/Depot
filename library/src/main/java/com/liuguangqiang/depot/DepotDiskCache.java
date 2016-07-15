package com.liuguangqiang.depot;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implemented a cache utils by DiskLruCache.
 * <p>
 * Created by Eric on 16/7/1.
 */
class DepotDiskCache {

    private DiskLruCache diskLruCache;

    DepotDiskCache(File cacheDir, int appVersion, long maxSize) throws IOException {
        diskLruCache = DiskLruCache.open(cacheDir, appVersion, 1, maxSize);
    }

    void put(String key, String value) {
        DiskLruCache.Editor editor;
        try {
            editor = diskLruCache.edit(Utils.getKey(key));
            if (editor != null) {
                if (writeEditor(value, editor)) {
                    diskLruCache.flush();
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void remove(String key) {
        try {
            diskLruCache.remove(get(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String get(String key) {
        String value = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(Utils.getKey(key));
            if (snapshot == null) {
                value = null;
            } else {
                value = snapshot.getString(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return value;
    }

    boolean contains(String key) {
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(Utils.getKey(key));
            if (snapshot == null) {
                return false;
            }
            snapshot.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean writeEditor(String value, DiskLruCache.Editor editor) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(editor.newOutputStream(0));
            outputStream.write(value.getBytes());

        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return true;
    }

}
