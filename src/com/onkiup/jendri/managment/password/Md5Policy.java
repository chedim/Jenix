package com.onkiup.jendri.managment.password;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.security.provider.MD5;

public class Md5Policy implements PasswordPolicy {
    @Override
    public String getName() {
        return "MD5";
    }

    @Override
    public String hash(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        return new String(hash);
    }
}
