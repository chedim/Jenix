package com.onkiup.jendri.models.user;

import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.Column;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.FieldNotNull;
import com.onkiup.jendri.db.annotations.Unique;

public class Profile extends Record {
    @Column.Indexed
    private String email;

    @Column.Indexed
    private String nickname;

    @Override
    public boolean willBeSaved() throws Exception {
        if (id == null) {
            Profile stored = getOwner().getProfile();
            boolean profileDuplicated = stored != null && !stored.equals(this);
            boolean emailDuplicated = email != null && Query.from(Profile.class).where("email = ?", email).hasMoreThan(0);
            return !(profileDuplicated || emailDuplicated);
        }
        return true;
    }
}
