package com.onkiup.jendri.access;

import java.lang.reflect.Field;

import com.onkiup.jendri.db.Record;

public class AccessServiceImpl implements AccessService {

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public boolean isReadable(User user, Record object) {
        User owner = object.getOwner();
        if (owner != null && owner.equals(user)) {
            return true;
        }

        Ability.Owned ownerAbilityAnnotation = object.getClass().getAnnotation(Ability.Owned.class);
        Ability.Read readAbilityAnnotation = object.getClass().getAnnotation(Ability.Read.class);
        return checkNonOwnerAbilities(user, ownerAbilityAnnotation, readAbilityAnnotation);
    }

    @Override
    public boolean isWritable(User user, Record object) {
        User owner = object.getOwner();
        if (owner != null && owner.equals(user)) {
            return true;
        }

        Ability.Owned ownerAbilityAnnotation = object.getClass().getAnnotation(Ability.Owned.class);
        Ability.Write writeAnnotation = object.getClass().getAnnotation(Ability.Write.class);
        return checkNonOwnerAbilities(user, ownerAbilityAnnotation, writeAnnotation);
    }

    @Override
    public boolean isReadable(User user, Record object, Field field) {
        User owner = object.getOwner();
        if (owner != null && owner.equals(owner)) {
            return true;
        }

        Ability.Owned ownerAbilityAnnotation = field.getAnnotation(Ability.Owned.class);
        Ability.Read readAnnotation = field.getAnnotation(Ability.Read.class);
        return checkNonOwnerAbilities(user, ownerAbilityAnnotation, readAnnotation);
    }

    @Override
    public boolean isWritable(User user, Record object, Field field) {
        User owner = object.getOwner();
        if (owner != null && owner.equals(owner)) {
            return true;
        }

        Ability.Owned ownerAbilityAnnotation = field.getAnnotation(Ability.Owned.class);
        Ability.Write writeAnnotation = field.getAnnotation(Ability.Write.class);
        return checkNonOwnerAbilities(user, ownerAbilityAnnotation, writeAnnotation);
    }

    private boolean checkNonOwnerAbilities(User user, Ability.Owned ownedAnnotation, Ability.Read readAnnotation) {
        if (readAnnotation != null) {
            String readAbility = readAnnotation.value();
            if (user != null && user.hasAbility(readAbility)) {
                return true;
            } else {
                return false;
            }
        } else if (ownedAnnotation != null) {
            return false;
        }
        // object is not limited
        return true;
    }

    private boolean checkNonOwnerAbilities(User user, Ability.Owned ownedAnnotation, Ability.Write writeAnnotation) {
        if (writeAnnotation != null) {
            String readAbility = writeAnnotation.value();
            if (user != null && user.hasAbility(readAbility)) {
                return true;
            } else {
                return false;
            }
        } else if (ownedAnnotation != null) {
            return false;
        }
        // object is not limited
        return true;
    }
}
