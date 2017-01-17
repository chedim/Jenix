package com.onkiup.jendri.config;

import java.util.Map;

public interface ConfigurationSource {
    Map<String, String> getEntries();
}
