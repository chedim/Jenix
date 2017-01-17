package com.onkiup.jendri.api;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.onkiup.jendri.access.Ability;
import com.onkiup.jendri.access.AccessService;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.Where;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.util.OopUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SerializationServiceImpl implements SerializationService.Implementation {

    @Inject
    private static AccessService accessService;

    private static JsonFactory factory = new JsonFactory();

    Logger logger = LogManager.getLogger(getClass());

    @Override
    public void serialize(User user, Object object, Writer writer) {
        try {
            JsonGenerator jg = factory.createGenerator(writer);
            stringify(jg, user, object);
            jg.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void schematize(String pkg, User user, Class type, Writer writer) {
        try {
            JsonGenerator jg = factory.createGenerator(writer);
            List<Field> fields = OopUtils.getFields(type);
            jg.writeStartObject();
            for (Field field : fields) {
                jg.writeFieldName(field.getName());
                jg.writeStartObject();
                Class fieldType = field.getType();
                List<String> definition = new ArrayList<>();
                if (isPrimitive(fieldType)) {
                    PrimitiveType desc = PrimitiveType.get(fieldType);
                    if (desc == null) {
                        throw new Exception("Unable to schematize class " + fieldType.getName() + " for field " + field.getName());
                    }
                    jg.writeStringField("type", desc.type);
                    jg.writeStringField("min", desc.min);
                    jg.writeStringField("max", desc.max);
                    jg.writeStringField("regexp", desc.regexp);
                } else {
                    String endpoint = RequestParserService.getEndpoint(pkg, type);
                    if (endpoint != null) {
                        Where ann = field.getAnnotation(Where.class);
                        jg.writeStringField("type", "reference");
                        jg.writeStringField("class", endpoint);
                        if (ann != null) {
                            jg.writeStringField("where", ann.value());
                        }
                    }
                }
                Ability.Owned owned = field.getAnnotation(Ability.Owned.class);
                if (owned != null) {
                    jg.writeBooleanField("owned", true);
                }
                Ability.Read read = field.getAnnotation(Ability.Read.class);
                if (read != null) {
                    jg.writeStringField("read", read.value());
                }
                Ability.Write write = field.getAnnotation(Ability.Write.class);
                if (write != null) {
                    jg.writeStringField("write", write.value());
                }
                jg.writeEndObject();
            }
            jg.writeEndObject();
            jg.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T unserialize(User user, Class<T> type, Reader reader) {
        try {
            return overwrite(user, type.newInstance(), reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T overwrite(User user, T target, Reader reader) {
        try {
            JsonParser parser = factory.createParser(reader);
            parse(parser, user, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    private boolean isPrimitive(Object test) {
        return isPrimitive(test.getClass());
    }

    private boolean isPrimitive(Class type) {
        return ClassUtils.isPrimitiveOrWrapper(type) || type.equals(Date.class) || type.equals(String.class);
    }

    //
    private void stringify(JsonGenerator generator, User user, Object object) throws Exception {
        if (object == null) {
            generator.writeNull();
        } else if (isPrimitive(object)) {
            if (object instanceof Date) {
                generator.writeRawValue(String.valueOf(((Date) object).getTime()));
            } else if (object instanceof String || object instanceof StringBuilder || object instanceof StringWriter) {
                generator.writeString(object.toString());
            } else {
                generator.writeRawValue(object.toString());
            }
        } else if (object instanceof Collection) {
            generator.writeStartArray();
            for (Object item : (Collection) object) {
                stringify(generator, user, item);
            }
            generator.writeEndArray();
        } else if (object instanceof HashMap) {
            generator.writeStartObject();
            for (Object key : ((HashMap) object).keySet()) {
                if (key == null) {
                    generator.writeFieldName("null");
                } else {
                    generator.writeFieldName(key.toString());
                }
                stringify(generator, user, ((HashMap) object).get(key));
            }
            generator.writeEndObject();
        } else {
            if (object instanceof Record && !accessService.isReadable(user, (Record) object)) {
                generator.writeNull();
                return;
            }
            Class type = object.getClass();
            generator.writeStartObject();
            for (Field field : OopUtils.getFields(type)) {
                boolean accessible = field.isAccessible();
                if (object instanceof Record && !accessService.isReadable(user, (Record) object, field)) {
                    // skipping an unaccessible field
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(object);
                String name = field.getName();
                if (value == null) {
                    generator.writeNullField(name);
                } else if (isPrimitive(value)) {
                    if (value instanceof String) {
                        generator.writeStringField(name, (String) value);
                    } else {
                        generator.writeFieldName(field.getName());
                        if (value instanceof Date) {
                            value = ((Date) value).getTime();
                        }
                        generator.writeRawValue(value.toString());
                    }
                } else {
                    generator.writeFieldName(field.getName());
                    stringify(generator, user, value);
                }
                field.setAccessible(accessible);
            }
            generator.writeEndObject();
        }
    }

    private void parse(JsonParser parser, User user, Object target) throws Exception {
        routeComplexType(parser, user, target, parser.nextToken());
    }

    private void routeComplexType(JsonParser parser, User user, Object target, JsonToken token) throws Exception {
        if (token == JsonToken.START_ARRAY) {
            parseList(parser, user, target);
        } else if (token == JsonToken.START_OBJECT) {
            parseObject(parser, user, target);
        } else if (token == JsonToken.VALUE_NUMBER_INT) {
            parseReference(parser, user, target);
        } else {
            throw new RuntimeException("initial token expected to be START_OBJECT OR START_ARRAY or VALUE_NUMBER_INT, not " + token);
        }
    }

    private void parseReference(JsonParser parser, User user, Object target) throws Exception {
        //logger.info("-- reference --");
        Class targetClass = target.getClass();
        if (!PersistantObject.class.isAssignableFrom(targetClass)) {
            throw new RuntimeException(targetClass.toString() + " is not a PersistantObject");
        }
        Table table = Table.forJavaClass(targetClass);
        Long id = parser.getValueAsLong();
        table.populateObject(id, (PersistantObject) target);
    }

    private void parseObject(JsonParser parser, User user, Object target) throws Exception {
        JsonToken token;
        //logger.info("{");
        if (target instanceof Record && !accessService.isWritable(user, (Record) target)) {
            //logger.info(">>");
            fastForward(parser, JsonToken.END_OBJECT);
            return;
        }
        boolean isMap = target instanceof Map;
        Map<String, Field> fields = OopUtils.getFieldsMap(target.getClass());
        while (JsonToken.END_OBJECT != (token = parser.nextToken())) {
            String name = parser.getCurrentName();
            Field field = fields.get(name);
            if (field != null || isMap) {
                if (target instanceof Record && !accessService.isWritable(user, (Record) target, field)) {
                    token = parser.nextToken();
                    //logger.info(name + " >> ");
                    if (token.equals(JsonToken.START_ARRAY)) {
                        fastForward(parser, JsonToken.END_ARRAY);
                    } else if (token.equals(JsonToken.START_OBJECT)) {
                        fastForward(parser, JsonToken.END_OBJECT);
                    }
                    continue;
                }

                Class fieldType;
                if (isMap) {
                    fieldType = OopUtils.getBoundClass(target.getClass(), target.getClass().getSuperclass(), "V");
                    if (fieldType == null) {
                        fieldType = String.class;
                    }
                } else {
                    fieldType = field.getType();
                }
                Object value;
                if (isPrimitive(fieldType)) {
                    token = parser.nextToken();
                    if (Date.class.isAssignableFrom(fieldType)) {
                        value = new Date(parser.getLongValue());
                        //logger.info(name + " = " + value);
                    } else {
                        String jsonVal = parser.getValueAsString();
                        //logger.info(name + " = " + jsonVal);
                        value = createObject(fieldType, jsonVal);
                    }
                } else {
                    value = fieldType.newInstance();
                    //logger.info(name + " — object");
                    routeComplexType(parser, user, value, parser.nextToken());
                }
                if (isMap) {
                    ((Map) target).put(name, value);
                } else {
                    field.setAccessible(true);
                    field.set(target, value);
                }
            }
        }
        //logger.info("}");
    }

    private void parseList(JsonParser parser, User user, Object target) throws Exception {
        JsonToken token;
        //logger.info("[");
        if (!(target instanceof List)) {
            throw new RuntimeException("target is not a List");
        }
        Class targetType = OopUtils.getBoundClass(target.getClass(), target.getClass().getSuperclass(), "E");
        int index = 0;
        List list = (List) target;
        while (JsonToken.END_ARRAY != (token = parser.nextToken())) {
            Object value = index < list.size() ? list.get(index) : null;
            if (isPrimitive(targetType)) {
                if (Date.class.isAssignableFrom(targetType)) {
                    Long raw = parser.getLongValue();
                    //logger.info(index + " = " + raw);
                    value = new Date(raw);
                } else {
                    Object jsonVal = parser.getCurrentValue();
                    //logger.info(index + " = " + jsonVal);
                    value = createObject(targetType, jsonVal);
                }
            } else {
                if (value == null) {
                    value = targetType.newInstance();
                }
                routeComplexType(parser, user, value, token);
            }
            if (index < list.size()) {
                list.set(index, value);
            } else {
                list.add(value);
            }
            index++;
        }
        //logger.info("]");
    }

    private void fastForward(JsonParser parser, JsonToken until) throws Exception {
        JsonToken current;
        while (!until.equals(current = parser.nextToken())) {
            if (current.equals(JsonToken.START_ARRAY)) {
                fastForward(parser, JsonToken.END_ARRAY);
            } else if (current.equals(JsonToken.START_OBJECT)) {
                fastForward(parser, JsonToken.END_OBJECT);
            }
        }
    }

    private <T> T createObject(Class<T> type, Object param) throws NoSuchMethodException {
        T result = null;
        if (param == null) {
            return null;
        }

        if (type.isAssignableFrom(param.getClass())) {
            return (T) param;
        }

        try {
            Constructor<T> c = type.getConstructor(param.getClass());
            result = c.newInstance(param);
        } catch (Exception a) {
            try {
                Method valueOf = type.getMethod("valueOf", param.getClass());
                result = (T) valueOf.invoke(null, param);
            } catch (Exception b) {
                try {
                    Constructor c = type.getConstructor(String.class);
                    result = (T) c.newInstance(param.toString());
                } catch (Exception c) {
                    try {
                        Method fromString = type.getMethod("fromString", String.class);
                        result = (T) fromString.invoke(null, param.toString());
                    } catch (Exception d) {
                        throw new NoSuchMethodException();
                    }
                }
            }
        }

        return result;
    }

    private enum PrimitiveType {
        BYTE(Byte.class, "number", "-128", "127", null),
        SHORT(Short.class, "number", "-32768", "32767", null),
        INTEGER(Integer.class, "number", "-2147483648", "2147483647", null),
        LONG(Long.class, "number", "-9223372036854775808", "9223372036854775807", null),
        FLOAT(Float.class, "number", null, null, null),
        DOUBLE(Double.class, "number", null, null, null),
        BOOLEAN(Boolean.class, "boolean", null, null, null),
        CHAR(Character.class, "string", null, null, "."),
        STRING(String.class, "string", null, null, null),
        DATE(Date.class, "timestamp", null, null, null);

        private Class javaType;
        private String type;
        private String min, max;
        private String regexp;

        PrimitiveType(Class javaClass, String type, String min, String max, String regexp) {
            this.javaType = javaClass;
            this.type = type;
            this.min = min;
            this.max = max;
            this.regexp = regexp;
        }

        @Override
        public String toString() {
            return javaType + ":" + type;
        }

        static PrimitiveType get(Class type) {
            for (PrimitiveType value : PrimitiveType.values()) {
                if (value.javaType.isAssignableFrom(type)) {
                    return value;
                }
            }

            return null;
        }
    }
}
