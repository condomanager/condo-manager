package br.com.condo.manager.arch.service.security;

import br.com.condo.manager.arch.model.entity.security.SecurityPrivilege;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import br.com.condo.manager.arch.service.util.SearchSpecification;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityProfileDAO extends BaseSpringDataDAO<SecurityProfile, Long> {

    public SecurityProfile updateOrCreateIfNotExists(String name, Collection<SecurityPrivilege> securityPrivileges) {
        List<SearchParameter> params = Lists.newArrayList(new SearchParameter("name", SearchParameter.Operator.EQUAL, name));
        Optional<SecurityProfile> securityProfile = repository.findOne(new SearchSpecification<>(params));

        if(securityProfile.isPresent()) {
            SecurityProfile profile = securityProfile.get();
            profile.getSecurityPrivileges().addAll(securityPrivileges);
            return update(profile);
        } else {
            return persist(new SecurityProfile(name, securityPrivileges));
        }
    }
}