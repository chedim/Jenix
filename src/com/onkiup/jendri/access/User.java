package com.onkiup.jendri.access;

import java.util.ArrayList;
import java.util.List;

import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.Model;
import com.onkiup.jendri.db.annotations.Unique;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.models.user.Profile;

@Model.StoreAs("user")
public class User extends Record {
    private String identifier;

    @Ability.Owned
    @Ability.Read(Ability.ADMIN)
    private transient List<String> abilities;

    public User() {
    }

    public User(String identifier, List<String> abilities) throws Exception {
        User stored = Query.from(User.class).where("identifier = ?", identifier).fetchOne();
        if (stored != null) {
            Table.forJavaClass(User.class).populateObject(stored.getId(), this);
        } else {
            this.identifier = identifier;
            this.abilities = abilities;
        }
    }

    public boolean hasAbility(String testAbility) {
        try {
            for (String ability : getAbilities()) {
                if (ability.equals(testAbility)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to check abilities", e);
        }

        return false;
    }

    public List<String> getAbilities() throws Exception {
        if (abilities == null) {
            List<UserAbility> userAbilities = Query.from(UserAbility.class).where("user = ?", this).fetch();
            abilities = new ArrayList<>();
            userAbilities.forEach(userAbility -> abilities.add(userAbility.getAbility()));
        }

        return abilities;
    }

    public void grantAbility(User to, String ability) throws Exception {
        if (!(hasAbility(Ability.GRANT) || hasAbility(Ability.ADMIN))) {
            throw new IllegalAccessException("Cannot grant: don't have such ability");
        }
        new UserAbility(to, ability).save();
    }

    public void revokeAbility(User of, Ability ability) throws Exception {
        if (!(hasAbility(Ability.REVOKE) || hasAbility(Ability.ADMIN))) {
            throw new IllegalAccessException("Cannot revoke: don't have such ability");
        }

        Query.from(UserAbility.class).where("owner = ?", of).and("ability = ?", ability).fetchOne().delete();
    }

    public Profile getProfile() throws Exception {
        return Query.from(Profile.class).where("owner = ?", this).fetchOne();
    }
}
