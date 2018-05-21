package br.com.condo.manager.arch.service.security;

import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class SecurityCredentialsDAO extends BaseSpringDataDAO<SecurityCredentials, Long> {

    public Optional<SecurityCredentials> retrieve(String username) {
        Collection<SecurityCredentials> credentialsCollection = find(new SearchParameter("username", SearchParameter.Operator.EQUAL, username));
        if(credentialsCollection.isEmpty()) return Optional.empty();
        if(credentialsCollection.size() > 1) throw new RuntimeException("There is more than one user for the same username");

        return credentialsCollection.stream().findFirst();
    }

    public Optional<SecurityCredentials> retrieve(String username, String password) {
        return retrieve(username).filter(c -> c.getPassword().equals(password));
    }

}
