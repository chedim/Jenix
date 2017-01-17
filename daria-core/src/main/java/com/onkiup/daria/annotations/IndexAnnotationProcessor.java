package com.onkiup.daria.annotations;

import java.lang.annotation.Annotation;

import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.StorageIndex;
import com.onkiup.daria.StorageTable;

public class IndexAnnotationProcessor implements AnnotationProcessor<Index> {
    @Override
    public void processField(Index annotation, StorageColumn field) {
        String indexName = annotation.value();
        if (indexName == null || indexName.length() == 0) {
            indexName = field.getJavaField().getName() + "_idx";
        }
        StorageIndex index = field.getTable().getIndex(indexName);
        index.addColumn(field);
        field.getIndexes().add(index);
        field.getTable().getIndexes().add(index);
    }
}
