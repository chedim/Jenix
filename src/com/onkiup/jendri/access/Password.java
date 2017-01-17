package com.onkiup.jendri.access;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import com.onkiup.jendri.db.Record;

public class Password extends Record {
    private String value;

    private transient String type;
    private transient String salt;
    private transient String hash;

    public Password() {
    }

    public Password(String value) {
        this.value = value;
        parse();
    }

    public boolean validate(String password) {
        try {
            parse();
            String salted = password + salt;
            MessageDigest md = MessageDigest.getInstance(type);
            md.update(salted.getBytes());
            String digest = new String(md.digest());
            return digest.equals(hash);
        } catch (Exception e) {
            return false;
        }
    }

    private void parse() {
        if (type == null) {
            String[] parts = value.split(":");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid stored password");
            }

            type = parts[0];
            salt = parts[1];
            hash = parts[2];
        }
    }

    public static Password create(String type, String password) {
        try {
            Random r = new SecureRandom();
            byte[] saltBytes = new byte[32];
            r.nextBytes(saltBytes);
            String salt = new String(saltBytes);

            String salted = password + salt;

            MessageDigest md = MessageDigest.getInstance(type);
            md.update(salted.getBytes());
            String digest = new String(md.digest());

            Password pass = new Password(type + ":" + salt + ":" + digest);
            return pass;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create a password", e);
        }
    }
}
