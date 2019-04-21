package br.com.condo.manager.arch.security;

import br.com.condo.manager.arch.model.entity.security.AnonymousAuthentication;
import br.com.condo.manager.arch.model.entity.security.SecurityAuthentication;
import br.com.condo.manager.arch.model.entity.security.SecurityPrivilege;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.security.SecurityAuthenticationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    SecurityAuthenticationDAO securityAuthenticationDAO;

    @Override
    protected void additionalAuthenticationChecks(final UserDetails d, final UsernamePasswordAuthenticationToken auth) {
        // Nothing to do
    }

    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken authentication) {
        final String token = String.valueOf(authentication.getCredentials());

        SecurityAuthentication auth = (token == "anonymous") ? new AnonymousAuthentication() : securityAuthenticationDAO.retrieve(token).orElseThrow(() -> new UsernameNotFoundException("Invalid authorization token " + token));

        List<String> roles = auth.getSecurityCredentials().getSecurityProfiles().stream().map(SecurityProfile::getName).collect(Collectors.toList());
        List<String> authorities = auth.getSecurityCredentials().getSecurityProfiles().stream().flatMap(sp -> sp.getSecurityPrivileges().stream().map(SecurityPrivilege::getName)).collect(Collectors.toList());

        UserDetails user = User
                .builder()
                .username(auth.getSecurityCredentials().getUsername())
                .password(auth.getSecurityCredentials().getPassword())
                .roles(roles.toArray(new String[0]))
                .authorities(authorities.toArray(new String[0]))
                .build();
        return user;
    }
}
