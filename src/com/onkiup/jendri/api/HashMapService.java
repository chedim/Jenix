package com.onkiup.jendri.api;

import java.util.HashMap;

import com.onkiup.jendri.access.User;
import com.onkiup.jendri.service.ServiceStub;

public interface HashMapService extends ServiceStub {
    public HashMap<String, Object> toHashMap(User user, Object source);
}
