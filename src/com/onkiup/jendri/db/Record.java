package com.onkiup.jendri.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.onkiup.jendri.access.Ability;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.annotations.AutoIncrement;
import com.onkiup.jendri.db.annotations.FieldNotNull;
import com.onkiup.jendri.db.annotations.PrimaryKey;

public abstract class Record implements Fetchable, Modificable, Storageable {

    public Record() {
    }

    @PrimaryKey
    @FieldNotNull
    @AutoIncrement
    protected Long id;

    @Ability.Owned
    protected User owner;

    private transient HashMap<String, Object> OLD;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) throws Exception {
        this.owner = owner;
    }

    @Override
    public void save() throws Exception {
        Database.getInstance().save(this);
    }

    @Override
    public void saveImmediately() throws Exception {
        Database.getInstance().saveImmediately(this);
    }

    @Override
    public void delete() throws Exception {
        Database.getInstance().delete(this);
    }

    @Override
    public void deleteImmediately() throws Exception {
        Database.getInstance().deleteImmediately(this);
    }

    @Override
    public void store(HashMap<String, Object> storage, boolean diff) {
        String prefix = "";
        Model.Prefix prefixAnnotation = getClass().getAnnotation(Model.Prefix.class);
        if (prefixAnnotation != null) {
            prefix += prefixAnnotation.value();
        }

        Field[] fields = getClass().getFields();
        for (Field field: fields) {
            if (Modifier.isProtected(field.getModifiers())) {
                // Only protected values can go into database
                String fieldName = prefix;
                Model.StoreAs storeAs = field.getAnnotation(Model.StoreAs.class);
                if (storeAs != null) {
                    fieldName += storeAs.value();
                } else {
                    fieldName += field.getName();
                }


                try {
                    Object value = field.get(this);

                    storage.put(fieldName, value);
                } catch (IllegalAccessException e) {
                    // this is simply not possible
                }
            }
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        Class oClass = o.getClass();
        Class meClass = getClass();
        if (o == null || !(meClass.isAssignableFrom(oClass) || oClass.isAssignableFrom(meClass))) return false;

        Record that = (Record) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
