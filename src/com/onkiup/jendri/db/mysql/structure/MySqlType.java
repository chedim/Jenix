package com.onkiup.jendri.db.mysql.structure;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.structure.ConnectedResult;
import com.onkiup.jendri.db.mysql.exceptions.ItemNotFound;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.structure.Table;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public enum MySqlType implements DataType {
    BOOLEAN("TINYINT", Boolean.class, Convert::getBoolean, Parse::bool),
    TINYINT("TINYINT", Byte.class, Convert::getByte, Parse::toByte),
    SMALLINT("SMALLINT", Short.class, Convert::getShort, Parse::toShort),
    INTEGER("INTEGER", Integer.class, Convert::getInteger, Parse::toInt, "INT"),
    BIGINT("BIGINT", Long.class, Convert::getLong, Parse::toLong),
    DECIMAL("DECIMAL", BigDecimal.class, Convert::getBigDecimal, Parse::bigDecimal),
    FLOAT("FLOAT", Float.class, Convert::getFloat, Parse::toFloat),
    DOUBLE("DOUBLE", Double.class, Convert::getDouble, Parse::toDouble),
    BIT("BIT", BitSet.class, Convert::storeBitSet, Convert::getBitSet, Parse::bitSet),
    DATETIME("BIGINT", Date.class, Convert::storeDate, Convert::getDate, Parse::date),
    CHAR("CHAR", Character.class, Convert::getChar, Parse::character),
    TEXT("TEXT", String.class, Convert::getString, Parse::string, "VARCHAR", "LONGTEXT"),
    BLOB("BLOB", ByteBuffer.class, Convert::storeByteBuffer, Convert::getByteBuffer, Parse::byteBuffer),
    ENUM(Convert::enumNameProvider, Enum.class, Convert::storeEnum, Convert::getEnum, Parse::enumeration),
    FUNCTION("BLOB", Function.class, Convert::storeFunction, Convert::getFunction, Parse::function),
//    REFERENCE_LIST("LIST", Collection.class, Convert::storeReferenceList, Convert::getReferenceList, Parse::referenceList),
    REFERENCE(Convert::referenceNameProvider, Object.class, Convert::storeReference, Convert::getReference, Parse::reference);

    private String name;
    private Function nameProvider;
    private Class type;
    private Function<Object, Object> encoder;
    private Function decoder;
    private BiFunction<Class, String, Object> parser;
    private String[] aliases;

    private <T> MySqlType(Function<Class<T>, String> nameProvider, Class<T> type, Function<T, Object> encoder, Function<DecoderRequest, T> decoder, BiFunction<Class, String, T> parser, String... aliases) {
        this((String) null, type, encoder, decoder, parser, aliases);
        this.nameProvider = nameProvider;
        this.name = type.getSimpleName();
    }

    private <T> MySqlType(String name, Class<T> type, Function<T, Object> encoder, Function<DecoderRequest, T> decoder, BiFunction<Class, String, T> parser, String... aliases) {
        this.name = name;
        this.type = type;
        this.encoder = (Function<Object, Object>) encoder;
        this.decoder = decoder;
        this.aliases = aliases;
        this.parser = (BiFunction<Class, String, Object>) parser;
    }

    private <T> MySqlType(String name, Class<T> type, Function<DecoderRequest, T> decoder, BiFunction<Class, String, T> parser, String... aliases) {
        this(name, type, null, decoder, parser, aliases);
    }

    @Override
    public Object read(ConnectedResult set, int index, Class type, Long targetObjectId) {
        DecoderRequest request = new DecoderRequest(set, index, type, targetObjectId);
        return read(request);
    }

    public Object store(Object o) {
        if (encoder != null) {
            return encoder.apply(o);
        } else if (o != null) {
            return o.toString();
        } else {
            return "null";
        }
    }

    @Override
    public String getDeclaration() {
        if (nameProvider != null) {
            return null;
        }
        return name;
    }

    public boolean is(String test) {
        String name = this.getName();
        return name.equals(test) || aliases != null && ArrayUtils.contains(aliases, test);
    }

    public static DataType forDeclaration(String declaration) {
        String typename = declaration.toUpperCase();
        int bracketIndex = typename.indexOf('(');
        if (bracketIndex != -1) {
            typename = typename.substring(0, bracketIndex);
        }

        for (MySqlType sql : MySqlType.class.getEnumConstants()) {
            if (sql.is(typename)) {
                return sql;
            }
        }

        return null;
    }

    @Override
    public void update() {
        return;
    }

    @Override
    public boolean existsInDatabase() {
        return true;
    }

    @Override
    public boolean matchesDatabase() {
        return true;
    }

    public static DataType forType(Class type) {
        for (MySqlType sql : MySqlType.class.getEnumConstants()) {
            if (sql.getType().isAssignableFrom(type)) {
                return sql;
            }
        }

        return null;
    }

    public Class getType() {
        return type;
    }

    @Override
    public String getDeclaration(Class represented) {
        if (nameProvider != null) {
            return (String) nameProvider.apply(represented);
        }
        return name;
    }

    public String getName() {
        return name;
    }

    private class FetchRequest extends ConnectedResult {

        public FetchRequest(Database source, ResultSet result) {
            super(source, result);
        }

        public DataType getSqlType() {
            return MySqlType.this;
        }
    }

    private interface Convert {

        static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");

        static Byte getByte(DecoderRequest request) {
            try {
                Byte result = request.result.getResult().getByte(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Short getShort(DecoderRequest request) {
            try {
                Short result = request.result.getResult().getShort(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Integer getInteger(DecoderRequest request) {
            try {
                Integer result = request.result.getResult().getInt(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Long getLong(DecoderRequest request) {
            try {
                ResultSet resultSet = request.result.getResult();
                Long value = resultSet.getLong(request.index);
                if (resultSet.wasNull()) {
                    return null;
                }
                return value;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static BigDecimal getBigDecimal(DecoderRequest request) {
            try {
                BigDecimal result = request.result.getResult().getBigDecimal(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Float getFloat(DecoderRequest request) {
            try {
                Float result = request.result.getResult().getFloat(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Double getDouble(DecoderRequest request) {
            try {
                Double result = request.result.getResult().getDouble(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static BitSet getBitSet(DecoderRequest request) {
            try {
                InputStream in = request.result.getResult().getBinaryStream(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                byte[] bytes = IOUtils.toByteArray(in);
                return BitSet.valueOf(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static String storeBitSet(BitSet o) {
            return "b'" + o.toString() + "'";
        }

        static String storeDate(Date o) {
            return String.valueOf(o.getTime());
        }

        static Date getDate(DecoderRequest request) {
            try {
                Long ts = request.result.getResult().getLong(request.index);
                if (request.result.getResult().wasNull()) {
                    return null;
                }
                return new Date(ts);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static Character getChar(DecoderRequest request) {
            try {
                char[] chars = new char[1];
                String result = request.result.getResult().getString(request.index);
                if (result == null) {
                    return null;
                }
                result.getChars(0, 1, chars, 0);
                return chars[0];
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static String getString(DecoderRequest request) {
            try {
                return request.result.getResult().getString(request.index);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static String storeByteBuffer(ByteBuffer o) {
            return new String(o.array());
        }

        static ByteBuffer getByteBuffer(DecoderRequest request) {
            try {
                byte[] val = request.result.getResult().getBytes(request.index);
                if (val == null) return null;
                return ByteBuffer.wrap(val);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static String enumNameProvider(Class<? extends Enum> represents) {
            List<String> values = new ArrayList<>();
            for (Enum val : represents.getEnumConstants()) {
                values.add(val.name());
            }

            return "ENUM('" + StringUtils.join(values, "', '") + "')";
        }

        static String storeEnum(Enum value) {
            return value.name();
        }

        static Enum getEnum(DecoderRequest request) {
            try {
                String name = request.result.getResult().getString(request.index);
                if (name == null) return null;
                Class<Enum> clazz = request.target;
                for (Enum cnst : clazz.getEnumConstants()) {
                    if (cnst.name().equals(name)) {
                        return cnst;
                    }
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        static String referenceNameProvider(Class represents) {
            try {
                Table represented = Table.forJavaClass(represents, null);
                Table.Field primaryKey = represented.getPrimaryKey();
                return primaryKey.getType().getName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static String storeReference(Object value) {
            try {
                Class clazz = value.getClass();
                Table represented = Table.forJavaClass(clazz, null);
                Table.Field primaryKey = represented.getPrimaryKey();
                Object id = primaryKey.get(value);
                if (id == null) {
                    represented.save((PersistantObject) value);
                    id = primaryKey.get(value);
                }
                return id.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static PersistantObject getReference(DecoderRequest request) {
            try {
                ResultSet resultSet = request.result.getResult();
                ResultSetMetaData meta = resultSet.getMetaData();
                String columnName = meta.getColumnName(request.index);
                Class target = request.target;
                Table targetTable = Table.forJavaClass(target, request.result.getDatabase());
                DataType targetFieldType = targetTable.getPrimaryKey().getType();
                Object keyValue = targetFieldType.read(request);
                PersistantObject instance = targetTable.createInstance();
                targetTable.populateObject(keyValue, instance);
                return instance;
            } catch (ItemNotFound e) {
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static Boolean getBoolean(DecoderRequest request) {
            try {
                ResultSet resultSet = request.result.getResult();
                return resultSet.getBoolean(request.index);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static String storeFunction(Function function) {
            return null;
        }

        static Function getFunction(DecoderRequest request) {
            return null;
        }

        static String storeReferenceList(Collection collection) {
            return null;
        }

        static Collection getReferenceList(DecoderRequest request) {
            try {
                ResultSet resultSet = request.result.getResult();
                ResultSetMetaData meta = resultSet.getMetaData();
                String columnName = meta.getColumnName(request.index);
                Class target = request.target;
                Long targetId = request.targetObjectId;
                Table targetTable = Table.forJavaClass(target, request.result.getDatabase());
                DataType targetFieldType = targetTable.getPrimaryKey().getType();
                Object keyValue = targetFieldType.read(request);
                PersistantObject instance = targetTable.createInstance();
                targetTable.populateObject(keyValue, instance);
//                Table.ReferenceList referenceList =
//                return instance;
                return null;
            } catch (ItemNotFound e) {
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Object read(DecoderRequest request) {
        return this.decoder.apply(request);
    }

    private interface Parse {
        static BitSet bitSet(Class to, String v) {
            return BitSet.valueOf(v.getBytes());
        }

        static Date date(Class to, String v) {
            try {
                Long val = Long.valueOf(v);
                return new Date(val);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }

        static Character character(Class to, String v) {
            return v.charAt(0);
        }

        static ByteBuffer byteBuffer(Class to, String v) {
            return ByteBuffer.wrap(v.getBytes());
        }

        static Enum enumeration(Class to, String v) {
            return Enum.valueOf(to, v);
        }

        static Boolean bool(Class aClass, String s) {
            return new Boolean(s);
        }

        static Byte toByte(Class aClass, String s) {
            return Byte.valueOf(s);
        }

        static Short toShort(Class aClass, String s) {
            return Short.valueOf(s);
        }

        static Integer toInt(Class aClass, String s) {
            return Integer.valueOf(s);
        }

        static Long toLong(Class aClass, String s) {
            return Long.valueOf(s);
        }

        static BigDecimal bigDecimal(Class aClass, String s) {
            return new BigDecimal(s);
        }

        static Float toFloat(Class aClass, String s) {
            return Float.valueOf(s);
        }

        static Double toDouble(Class aClass, String s) {
            return Double.valueOf(s);
        }

        static String string(Class aClass, String s) {
            return s;
        }

        static Fetchable reference(Class c, String s) {
            try {
                Table table = Table.forJavaClass(c, null);
                Table.Field pk = table.getPrimaryKey();
                DataType pkType = pk.getType();
                Object key = pkType.parse(pk.getRepresentedField().getType(), s);
                return Database.getInstance().get(table.getRepresentedClass(), key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static Function function(Class aClass, String s) {
            return null;
        }

        static List<Fetchable> referenceList(Class c, String s) {
            try {
                Table table = Table.forJavaClass(c, null);
                Table.Field pk = table.getPrimaryKey();
                DataType pkType = pk.getType();
                Object key = pkType.parse(pk.getRepresentedField().getType(), s);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> T parse(Class<T> type, String s) {
        if (!this.type.isAssignableFrom(type)) {
            throw new InvalidParameterException("Type " + type.getName() + " cannot be parsed with " + this.toString());
        }
        return (T) parser.apply(type, s);
    }
}
