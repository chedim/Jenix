package com.onkiup.jendri.util;

import com.onkiup.jendri.service.Service;

public interface NameConvertor extends Service {
    String fromJava(String javaName);
    String toJava(String name);
}
