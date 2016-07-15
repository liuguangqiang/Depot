package com.liuguangqiang.depot;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by Eric on 16/7/7.
 */
public final class Utils {

    private static final String ALGORITHM = "MD5";
    private static final String FORMAT = "UTF-8";

    private Utils() {
    }

    public static String getKey(String key) {
        try {
            MessageDigest m = MessageDigest.getInstance(ALGORITHM);
            m.update(key.getBytes(FORMAT));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

}
