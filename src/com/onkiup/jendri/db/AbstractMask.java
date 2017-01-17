package com.onkiup.jendri.db;

import com.onkiup.jendri.type.TypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Represents a set of fields that can be fetched from database
 */
public abstract class AbstractMask<X extends Fetchable> implements Fetchable {

}
