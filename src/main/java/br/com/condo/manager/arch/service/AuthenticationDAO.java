package br.com.condo.manager.arch.service;

import br.com.condo.manager.arch.model.entity.Authentication;
import br.com.condo.manager.arch.model.entity.UserCredentials;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AuthenticationDAO extends BaseSpringDataDAO<Authentication, String> {

    public Optional<Authentication> retrieve(UserCredentials userCredentials) {
        LinkedList<SortParameter> sortParams = new LinkedList<>();
        sortParams.add(new SortParameter("loginDate", SortParameter.Order.DESC));
        Collection<Authentication> credentialsCollection = find(Stream.of(new SearchParameter("userCredentials.id", SearchParameter.Operator.EQUAL, userCredentials.getId())).collect(Collectors.toList()), sortParams);
        return credentialsCollection.stream().findFirst();
    }

}
