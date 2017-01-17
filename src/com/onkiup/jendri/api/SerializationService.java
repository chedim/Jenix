package com.onkiup.jendri.api;

import java.io.Reader;
import java.io.Writer;

import com.onkiup.jendri.access.User;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;

public final class SerializationService {
    @Inject
    static SerializationService.Implementation instance = null;

    interface Implementation extends ServiceStub {
        void serialize(User user, Object object, Writer writer);
        void schematize(String pkg, User user, Class type, Writer writer);
        <T> T unserialize(User user, Class<T> type, Reader reader);
        <T> T overwrite(User user, T target, Reader reader);
        String getContentType();
    }


    public static void serialize(User user, Object object, Writer writer) {
        instance.serialize(user, object, writer);
    }

    public static void schematize(String pkg, User user, Class type, Writer writer) {
        instance.schematize(pkg, user, type, writer);
    }

    public static <T> T unserialize(User user, Class<T> type, Reader reader) {
        return instance.unserialize(user, type, reader);
    }

    public static <T> T overwrite(User user, T target, Reader reader) {
        return instance.overwrite(user, target, reader);
    }

    public static String getContentType() {
        return instance.getContentType();
    }
}