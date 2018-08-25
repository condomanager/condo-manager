package br.com.condo.manager.arch.security;

import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    @Autowired
    private SecurityCredentialsDAO securityCredentialsDAO;

    @Nullable
    public SecurityCredentials authenticatedCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
                return securityCredentialsDAO.retrieve(authentication.getName()).orElse(null);
        } catch (Exception e) {

        }
        return null;
    }

}
