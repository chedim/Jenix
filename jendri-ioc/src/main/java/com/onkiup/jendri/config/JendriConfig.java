package com.onkiup.jendri.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JendriConfig {
    protected static final HashMap<String, String> entries = new HashMap<>();

    static {
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> param : env.entrySet()) {
            entries.put(param.getKey(), param.getValue());
        }

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL config = loader.getResource("j.json");
            Reader input = new InputStreamReader(config.openStream());
            JsonParser parser = new JsonFactory().createParser(input);
            JsonToken token = parser.nextToken();
            while (JsonToken.END_OBJECT != (token = parser.nextToken())) {
                String name = parser.getCurrentName();
                parser.nextToken();
                String value = parser.getValueAsString();
                entries.put(name, value);
            }
            parser.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    public static String getConfigValue(String key, String defaultValue) {
        if (entries.containsKey(key)) {
            return entries.get(key);
        } else {
            return defaultValue;
        }
    }

    public static void processContext(ServletContext context) {
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            entries.put(name, context.getInitParameter(name));
        }
    }

    public static void setConfigValue(String key, String value) {
        entries.put(key, value);
    }

    public static Stream<String> match(String prefix) {
        return entries.keySet().stream().filter(s -> s.startsWith(prefix));
    }

    public static Stream<String> matchLevels(String prefix, int levels) {
        return match(prefix).filter(s -> s.split("\\.").length == levels + 1).distinct();
    }
}
