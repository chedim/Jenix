package com.onkiup.jendri.access;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.injection.Injector;
import com.onkiup.jendri.service.Service;
import com.onkiup.jendri.service.ServiceStub;

public interface AccessService extends ServiceStub {
    boolean isReadable(User user, Record object);
    boolean isWritable(User user, Record object);
    boolean isReadable(User user, Record object, Field field);
    boolean isWritable(User user, Record object, Field field);
}
