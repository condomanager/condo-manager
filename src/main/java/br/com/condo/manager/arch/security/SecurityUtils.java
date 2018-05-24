package br.com.condo.manager.arch.security;

import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    @Autowired
    private static SecurityCredentialsDAO securityCredentialsDAO;

    @Nullable
    public static SecurityCredentials authenticatedCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
            return securityCredentialsDAO.retrieve(authentication.getName()).orElse(null);
        return null;
    }

}
