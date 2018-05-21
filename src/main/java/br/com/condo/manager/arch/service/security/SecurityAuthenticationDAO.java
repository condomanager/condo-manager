package br.com.condo.manager.arch.service.security;

import br.com.condo.manager.arch.model.entity.security.SecurityAuthentication;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import br.com.condo.manager.arch.service.util.SortParameter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SecurityAuthenticationDAO extends BaseSpringDataDAO<SecurityAuthentication, String> {

    public Optional<SecurityAuthentication> retrieve(SecurityCredentials securityCredentials) {
        LinkedList<SortParameter> sortParams = new LinkedList<>();
        sortParams.add(new SortParameter("loginDate", SortParameter.Order.DESC));
        Collection<SecurityAuthentication> credentialsCollection = find(Stream.of(new SearchParameter("securityCredentials.id", SearchParameter.Operator.EQUAL, securityCredentials.getId())).collect(Collectors.toList()), sortParams);
        return credentialsCollection.stream().findFirst();
    }

}
