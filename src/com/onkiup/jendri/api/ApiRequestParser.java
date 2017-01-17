package com.onkiup.jendri.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.structure.Table;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Regexp;

public class ApiRequestParser {
    private String path;
    private Long id;
    private Map<String, String[]> params;
    private Table table;
    private String pkg;
    private Class type;

    public static final Pattern ID_PATTERN = Pattern.compile(".*\\/(\\d+)$");

    public ApiRequestParser(String servletPath, String pkg, HttpServletRequest request) {
        try {
            if (!pkg.endsWith(".")) {
                pkg += ".";
            }
            this.pkg = pkg;
            params = request.getParameterMap();
            URL rqUrl = new URL(request.getRequestURL().toString());
            path = rqUrl.getPath().substring(servletPath.length());
            Matcher m = ID_PATTERN.matcher(path);
            if (m.matches()) {
                String idString = m.group(1);
                id = Long.valueOf(idString);
                path = path.substring(0, path.length() - idString.length() - 1);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiRequestParser(Class type, String filter) {
        params = HttpUtils.parseQueryString(filter);
        this.type = type;
    }

    public Class getResultType() throws ClassNotFoundException {
        if (type == null) {
            String className = path.replaceAll("\\/", ".");

            if (className.startsWith(".")) {
                className = className.substring(1);
            }

            if (className.endsWith(".")) {
                className = className.substring(0, className.length() - 1);
            }

            return Class.forName(pkg + className);
        } else {
            return type;
        }
    }

    public QueryBuilder getQuery() throws Exception {
        Class target = getResultType();
        table = Table.forJavaClass(target, null);
        QueryBuilder builder = Query.from(target);
        if (id != null) {
            builder.where("id = ?", id);
        }
        for (String key : params.keySet()) {
            Clause clause = new Clause(key, params.get(key));
            builder.and(clause.toString());
        }

        return builder;
    }

    public boolean hasId() {
        return id != null;
    }

    private class Clause {
        String[] fields;
        String[] commands;
        String[] values;

        public Clause(String clause, String[] values) {
            this.values = values;
            String[] parts = clause.split(":");
            if (parts.length > 1) {
                commands = new String[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    commands[i - 1] = parts[i];
                }
            } else {
                commands = new String[]{"eq"};
            }

            String fieldsList = parts[0];
            if (fieldsList.startsWith("(") && fieldsList.endsWith(")")) {
                fieldsList = fieldsList.substring(1, fieldsList.length() - 1);
                fields = fieldsList.split(",");
            } else {
                fields = new String[]{fieldsList};
            }
        }

        @Override
        public String toString() {
            List<String> orFields = new ArrayList<>();
            for (String fieldName : fields) {
                if (fieldName.endsWith("[]")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 2);
                }
                Table.Field field = table.getField(fieldName);
                List<String> orCommands = new ArrayList<>();
                for (String command : commands) {
                    command = command.toUpperCase();
                    if (command.endsWith("[]")) {
                        command = command.substring(0, command.length() - 2);
                    }
                    Commands processor = Commands.valueOf(command);
                    orCommands.add(processor.process(field, values));
                }
                orFields.add(StringUtils.join(orCommands, " OR "));
            }

            return StringUtils.join(orFields, " OR ");
        }
    }

    private enum Commands {
        IN((field, values) -> {
            if (values.length == 0) {
                throw new RuntimeException("IN arguments weren't provided");
            }

            DataType type = field.getType();
            List<String> storedValues = new ArrayList<>();
            for (String value : values) {
                storedValues.add(processValue(field, value));
            }

            return field.getName() + " IN (" + StringUtils.join(storedValues, ",") + ")";
        }),
        NOTIN((field, values) -> {
            if (values.length == 0) {
                throw new RuntimeException("IN arguments weren't provided");
            }

            DataType type = field.getType();
            List<String> storedValues = new ArrayList<>();
            for (String value : values) {
                storedValues.add(processValue(field, value));
            }

            return field.getName() + " NOT IN (" + StringUtils.join(storedValues, ",") + ")";
        }),
        EQ((field, values) -> {
            if (values.length > 1) {
                return Commands.IN.process(field, values);
            } else if (values.length == 0) {
                throw new RuntimeException("EQ argument wasn't provided");
            }

            return field.getName() + " = " + processValue(field, values[0]);
        }),
        NOT((field, values) -> {
            if (values.length > 1) {
                return Commands.NOTIN.process(field, values);
            } else if (values.length == 0) {
                throw new RuntimeException("NOT argument wasn't provided");
            }

            return field.getName() + " != " + processValue(field, values[0]);
        }),
        GT((field, values) -> {
            return binaryOperator(field, values, ">");
        }),
        LT((field, values) -> {
            return binaryOperator(field, values, "<");
        }),
        GTE((field, values) -> {
            return binaryOperator(field, values, ">=");
        }),
        LTE((field, values) -> {
            return binaryOperator(field, values, "<=");
        }),
        NULL((field, strings) -> {
            if (strings.length > 0 && strings[0].equals("false")) {
                return field.getName() + " IS NOT NULL";
            } else {
                return field.getName() + " IS NULL";
            }
        }),
        BETWEEN((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                String[] parts = value.split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("Invalid operands number for RANGE on field " + field.getName());
                }

                ors.add(field.getName() + " BETWEEN " + processValue(field, parts[0]) + " AND " + processValue(field, parts[1]));
            }

            return StringUtils.join(ors, " OR ");
        }),
        OUTSIDE((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                String[] parts = value.split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("Invalid operands number for RANGE on field " + field.getName());
                }

                ors.add("(NOT (" + field.getName() + " BETWEEN " + processValue(field, parts[0]) + " AND " + processValue(field, parts[1]) + "))");
            }

            return StringUtils.join(ors, " AND ");
        }),
        PREFIX((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                ors.add(field.getName() + " LIKE " + processValue(field, value) + " \"%\"");
            }

            return StringUtils.join(ors, " OR ");
        }),
        LIKE((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                ors.add(field.getName() + " LIKE " + "\"%\" " + processValue(field, value) + "\"%\"");
            }

            return StringUtils.join(ors, " AND ");
        }),
        NOTLIKE((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                ors.add(field.getName() + " NOT LIKE " + "\"%\" " + processValue(field, value) + "\"%\"");
            }

            return StringUtils.join(ors, " AND ");
        }),
        SUFFIX((field, values) -> {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                ors.add(field.getName() + " LIKE " + "\"%\" " + processValue(field, value));
            }

            return StringUtils.join(ors, " OR ");
        });

        private BiFunction<Table.Field, String[], String> operator;

        Commands(BiFunction<Table.Field, String[], String> operator) {
            this.operator = operator;
        }

        public String process(Table.Field[] fields, String[] values) {
            List<String> result = new ArrayList<>();
            for (Table.Field field : fields) {
                result.add(process(field, values));
            }
            if (result.size() == 0) {
                return "TRUE";
            } else {
                return "(" + StringUtils.join(result, ") OR (") + ")";
            }
        }

        public String process(Table.Field field, String[] values) {
            return operator.apply(field, values);
        }

        private static String processValue(Table.Field field, String source) {
            DataType type = field.getType();

            Object value = type.parse(field.getRepresentedField().getType(), source);

            Object stored = type.store(value);
            return stored instanceof String ? "'" + stored + "'" : stored.toString();
        }

        private static String binaryOperator(Table.Field field, String[] values, String operator) {
            List<String> ors = new ArrayList<>();
            for (String value : values) {
                ors.add(field.getName() + " " + operator + " " + processValue(field, value));
            }

            return StringUtils.join(ors, " OR ");
        }
    }
}
