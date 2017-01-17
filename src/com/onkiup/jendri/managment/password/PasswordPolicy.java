package com.onkiup.jendri.managment.password;

import java.security.NoSuchAlgorithmException;

import com.onkiup.jendri.injection.Inject;

public interface PasswordPolicy {
    public String getName();

    public String hash(String password) throws Exception;
}
