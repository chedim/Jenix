package com.onkiup.jendri.access.oauth;

import java.util.Date;

import com.onkiup.jendri.access.Ability;
import com.onkiup.jendri.db.Record;

@Ability.Owned
@Ability.Read(Ability.ADMIN)
@Ability.Write(Ability.ADMIN)
public class OAuthClient extends OAuthToken {
    private String name;
    private String description;
    private String website;
    private String loginRedirect;
    private String clientId;

    @Ability.Write(Ability.ADMIN)
    private Date approved;
    @Ability.Write(Ability.ADMIN)
    private boolean isTrusted;


}
