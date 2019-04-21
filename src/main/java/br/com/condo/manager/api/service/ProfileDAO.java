package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import br.com.condo.manager.arch.service.security.SecurityProfileDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class ProfileDAO extends BaseSpringDataDAO<Profile, Long> {

    @Autowired
    private SecurityCredentialsDAO securityCredentialsDAO;

    @Autowired
    private SecurityProfileDAO securityProfileDAO;

    public boolean checkAvailability(String username) {
        return securityCredentialsDAO.checkAvailability(username);
    }

    @Override
    public Profile create(Profile entity) {
        SearchParameter securityProfilesByNames = new SearchParameter("name", SearchParameter.Operator.IN, entity.getSecurityProfiles());
        Collection<SecurityProfile> securityProfiles = securityProfileDAO.find(securityProfilesByNames);
        SecurityCredentials securityCredentials = createSecurityCredentials(entity.getUsername(), entity.getPassword(), securityProfiles);

        entity.setId(securityCredentials.getId());
        return super.create(entity);
    }

    public SecurityCredentials createSecurityCredentials(String username, String password, Collection<SecurityProfile> securityProfiles) {
        SecurityCredentials securityCredentials = new SecurityCredentials(username, password);
        securityCredentials.setEnabled(true);
        securityCredentials.setSecurityProfiles(securityProfiles);
        return securityCredentialsDAO.create(securityCredentials);
    }

    public Profile createIfNotExists(Profile profile) {
        Optional<SecurityCredentials> securityCredentials = securityCredentialsDAO.retrieve(profile.getUsername());
        if (!securityCredentials.isPresent())
            return create(profile);

        return null;
    }
}
