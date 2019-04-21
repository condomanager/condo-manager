package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import br.com.condo.manager.arch.service.security.SecurityProfileDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class ProfileDAO extends BaseSpringDataDAO<Profile, Long> {

    @Autowired
    private SecurityCredentialsDAO securityCredentialsDAO;

    @Autowired
    private SecurityProfileDAO securityProfileDAO;

    public boolean checkAvailability(String username) {
        return securityCredentialsDAO.checkAvailability(username);
    }

    protected Set<SecurityProfile> getSecurityProfilesByNames(Collection<String> names) {
        SearchParameter securityProfilesByNames = new SearchParameter("name", SearchParameter.Operator.IN, names);
        Collection<SecurityProfile> securityProfiles = securityProfileDAO.find(securityProfilesByNames);
        return Sets.newHashSet(securityProfiles);
    }

    @Override
    public Profile create(Profile entity) {
        SecurityCredentials securityCredentials = createSecurityCredentials(entity.getCpf(), entity.getPassword(), getSecurityProfilesByNames(entity.getSecurityProfiles()));
        entity.setId(securityCredentials.getId());
        return super.create(entity);
    }

    @Override
    public Profile update(Profile entity) {
        SecurityCredentials securityCredentials = updateSecurityCredentials(entity.getId(), entity.getCpf(), entity.getPassword(), getSecurityProfilesByNames(entity.getSecurityProfiles()));
        return super.update(entity);
    }

    public SecurityCredentials createSecurityCredentials(String username, String password, Set<SecurityProfile> securityProfiles) {
        SecurityCredentials securityCredentials = new SecurityCredentials(username, password);
        securityCredentials.setEnabled(true);
        securityCredentials.setSecurityProfiles(securityProfiles);
        return securityCredentialsDAO.create(securityCredentials);
    }

    public SecurityCredentials updateSecurityCredentials(Long id, String username, String password, Set<SecurityProfile> securityProfiles) {
        SecurityCredentials securityCredentials = securityCredentialsDAO.retrieve(id).get();

        boolean shouldChangeUsername = !username.equals(securityCredentials.getUsername());
        boolean shouldChangePassword = password != null && !password.trim().isEmpty();
        if (shouldChangeUsername || shouldChangePassword) {
            if (shouldChangeUsername)
                securityCredentials.setUsername(username);
            if (shouldChangePassword)
                securityCredentials.setPassword(password);
            return securityCredentialsDAO.update(securityCredentials);
        }

        return securityCredentials;
    }

    public Profile createIfNotExists(Profile profile) {
        if (checkAvailability(profile.getCpf()))
            return create(profile);

        return null;
    }
}
