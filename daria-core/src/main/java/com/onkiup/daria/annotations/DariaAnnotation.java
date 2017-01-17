package com.onkiup.daria.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;

import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.StorageTable;
import static jdk.nashorn.internal.runtime.Debug.id;

@Retention(RetentionPolicy.RUNTIME)
public @interface DariaAnnotation {
    Class<? extends AnnotationProcessor> value();

    class Static {

        private static HashMap<String, AnnotationProcessor> processors = new HashMap<>();

        public static void processType(StorageTable table) {
            Class from = table.getJavaClass();
            Annotation[] annotations = (Annotation[]) from.getAnnotations();
            for (Annotation annotation : annotations) {
                Class annClass = annotation.getClass();
                String annClassName = annClass.getAnnotatedInterfaces()[0].getType().getTypeName();
                if (annClassName != null) {
                    if (!processors.containsKey(annClassName)) {
                        try {
                            annClass = Class.forName(annClassName);
                            DariaAnnotation dariaAnnotation = (DariaAnnotation) annClass.getAnnotation(DariaAnnotation.class);
                            if (dariaAnnotation != null) {
                                Class<? extends AnnotationProcessor> processorClass = dariaAnnotation.value();
                                processors.put(annClassName, AnnotationProcessor.Static.getInstance(processorClass));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    AnnotationProcessor processor = processors.get(annClassName);
                    if (processor != null) {
                        processor.processType(annotation, table);
                    }
                }
            }
        }

        public static void processField(StorageColumn column) {
            Field from = column.getJavaField();
            Annotation[] annotations = (Annotation[]) from.getAnnotations();
            for (Annotation annotation : annotations) {
                Class annClass = annotation.getClass();
                DariaAnnotation dariaAnnotation = (DariaAnnotation) annClass.getAnnotation(DariaAnnotation.class);
                if (dariaAnnotation != null) {
                    Class<? extends AnnotationProcessor> processorClass = dariaAnnotation.value();
                    AnnotationProcessor processor = AnnotationProcessor.Static.getInstance(processorClass);
                    processor.processField(annotation, column);
                }
            }
        }
    }
}
