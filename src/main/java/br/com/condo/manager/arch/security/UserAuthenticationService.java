package br.com.condo.manager.arch.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthenticationService {

    Map<String, UserDetails> users = new HashMap<>();

    public Optional<String> login(final String username, final String password) {
        final String token = UUID.randomUUID().toString();
        final UserDetails userDetails = User
                .builder()
                .username(username)
                .password(password)
                .build();

        users.put(token, userDetails);
        return Optional.of(token);
    }

    public Optional<UserDetails> findByToken(final String token) {
        return Optional.ofNullable(users.get(token));
    }

    public void logout(final String token) {
        users.remove(token);
    }

}
