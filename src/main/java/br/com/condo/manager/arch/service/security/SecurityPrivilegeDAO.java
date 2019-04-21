package br.com.condo.manager.arch.service.security;

import br.com.condo.manager.arch.model.entity.security.SecurityPrivilege;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import br.com.condo.manager.arch.service.util.SearchSpecification;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityPrivilegeDAO extends BaseSpringDataDAO<SecurityPrivilege, Long> {

    public SecurityPrivilege updateOrCreateIfNotExists(String name) {
        List<SearchParameter> params = Lists.newArrayList(new SearchParameter("name", SearchParameter.Operator.EQUAL, name));
        Optional<SecurityPrivilege> securityPrivilege = repository.findOne(new SearchSpecification<>(params));

        return securityPrivilege.isPresent() ? securityPrivilege.get() : persist(new SecurityPrivilege(name));
    }

}