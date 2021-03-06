package br.com.condo.manager.arch.model.entity.security;

import java.util.HashSet;

public class AnonymousAuthentication extends SecurityAuthentication {

    public AnonymousAuthentication() {
        super(new SecurityCredentials(0L,"anonymous", "anonymous"));
        this.getSecurityCredentials().setSecurityProfiles(new HashSet<>());
    }
}
