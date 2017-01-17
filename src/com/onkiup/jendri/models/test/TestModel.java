package com.onkiup.jendri.models.test;

import java.util.Date;

import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.Index;
import com.onkiup.jendri.db.annotations.Indexes;
import com.onkiup.jendri.db.annotations.Where;

@Indexes({
        @Index({"aShort"})
})
public class TestModel extends Record {
    protected Short aShort;
    @Where("test:null")
    protected TestModel test;
    protected String text;
    protected Date date = new Date();
}
