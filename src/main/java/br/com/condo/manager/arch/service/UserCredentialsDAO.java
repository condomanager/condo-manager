package br.com.condo.manager.arch.service;

import br.com.condo.manager.arch.model.entity.UserCredentials;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserCredentialsDAO extends BaseSpringDataDAO<UserCredentials, Long> {

    public Optional<UserCredentials> retrieve(String username, String password) {
        Collection<UserCredentials> credentialsCollection = find(new SearchParameter("username", SearchParameter.Operator.EQUAL, username));

        if(credentialsCollection.isEmpty()) return Optional.empty();

        if(credentialsCollection.size() > 1) throw new RuntimeException("There is more than one user for the same username");

        return credentialsCollection.stream().filter(c -> c.getPassword().equals(password)).findAny();
    }

}
