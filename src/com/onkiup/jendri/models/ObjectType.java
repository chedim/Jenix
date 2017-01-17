package com.onkiup.jendri.models;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.onkiup.jendri.api.AbstractApi;
import com.onkiup.jendri.api.DynamicObject;
import com.onkiup.jendri.cms.ui.annotations.Searchable;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.util.OopUtils;
import com.onkiup.jendri.util.StringUtils;
import com.sun.scenario.effect.Filterable;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class ObjectType extends ArrayList<ObjectType.ObjectTypeInfo> implements DynamicObject {

    @Inject
    private static AbstractApi api;
    private static List<ObjectTypeInfo> classes;

    public ObjectType() throws IOException {
        if (classes == null || classes.size() == 0) {
            try {
                ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
                classes = cp.getTopLevelClassesRecursive(api.getPackage()).stream()
                        .map(c -> {
                            try {
                                return Class.forName(c.getName());
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(c -> Fetchable.class.isAssignableFrom(c) || DynamicObject.class.isAssignableFrom(c))
                        .map(ObjectTypeInfo::new)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.addAll(classes);
    }

    public static class ObjectTypeInfo {
        private String type;
        private String label;
        private boolean searchable;

        private ObjectTypeInfo(Class c) {
            type = c.getName();
            label = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(c.getSimpleName()), " ");
            searchable = c.getAnnotation(Searchable.class) == null;
        }
    }
}
