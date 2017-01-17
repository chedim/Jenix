package com.onkiup.ai;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onkiup.jendri.db.Record;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {

}
