package com.onkiup.jendri.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
    public static String hash(String algorithm, String value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.update(value.getBytes());
        return String.format("%032x", new BigInteger(1, digest.digest()));
    }
}
