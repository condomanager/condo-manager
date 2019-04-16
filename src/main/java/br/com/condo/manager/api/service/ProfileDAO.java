package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ProfileDAO extends BaseSpringDataDAO<Profile, Long> {

    @Autowired
    private SecurityCredentialsDAO securityCredentialsDAO;

    public boolean checkAvailability(String username) {
        return securityCredentialsDAO.checkAvailability(username);
    }

    public SecurityCredentials createSecurityCredentials(String username, String password, Collection<SecurityProfile> securityProfiles) {
        SecurityCredentials securityCredentials = new SecurityCredentials(username, password);
        securityCredentials.setEnabled(true);
        securityCredentials.setSecurityProfiles(securityProfiles);
        return securityCredentialsDAO.create(securityCredentials);
    }

    public SecurityCredentials createSecurityCredentials(String username, String password) {
        return createSecurityCredentials(username, password, null);
    }
}
