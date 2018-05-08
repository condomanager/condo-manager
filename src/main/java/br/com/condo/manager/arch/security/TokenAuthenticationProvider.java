package br.com.condo.manager.arch.security;

import br.com.condo.manager.arch.model.entity.Authentication;
import br.com.condo.manager.arch.service.AuthenticationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    AuthenticationDAO authenticationDAO;

    @Override
    protected void additionalAuthenticationChecks(final UserDetails d, final UsernamePasswordAuthenticationToken auth) {
        // Nothing to do
    }

    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken authentication) {
        final String token = String.valueOf(authentication.getCredentials());

        Authentication auth = authenticationDAO.retrieve(token).orElseThrow(() -> new UsernameNotFoundException("Invalid authentication token " + token));

        return User
                .builder()
                .username(auth.getUserCredentials().getUsername())
                .password(auth.getUserCredentials().getPassword())
                .authorities("USER")
                .build();
    }
}
