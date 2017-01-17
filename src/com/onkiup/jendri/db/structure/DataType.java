package com.onkiup.jendri.db.structure;

import java.lang.reflect.Method;
import java.util.function.Function;

import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.mysql.structure.MySqlType;
import com.onkiup.jendri.injection.Inject;

public interface DataType extends CustomSqlEntity<Class> {

    Object read(DecoderRequest request);

    <T> T parse(Class<T> type, String s);

    Object store(Object primaryKey);

    <T> T read(ConnectedResult result, int i, Class<T> type, Long targetObjectId);

    static DataType forType(Class<?> aClass) {
        try {
            if (Impl.forType == null) {
                Impl.forType = Impl.dataType.getMethod("forType", Class.class);
            }

            return (DataType) Impl.forType.invoke(null, aClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class DecoderRequest {
        public ConnectedResult result;
        public int index;
        public Class target;
        public Long targetObjectId;

        public DecoderRequest(ConnectedResult result, int index, Class target, Long targetObjectId) {
            this.result = result;
            this.index = index;
            this.target = target;
            this.targetObjectId = targetObjectId;
        }
    }

    static class Impl {
        @Inject
        public static Class<? extends DataType> dataType;

        public static Method forType;
    }
}
