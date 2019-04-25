package br.com.condo.manager;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.arch.model.entity.security.SecurityPrivilege;
import br.com.condo.manager.arch.model.entity.security.SecurityProfile;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.security.SecurityPrivilegeDAO;
import br.com.condo.manager.arch.service.security.SecurityProfileDAO;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SystemDataStarter implements ApplicationListener<ContextRefreshedEvent> {

    protected Logger LOGGER = LoggerFactory.getLogger(BaseSpringDataDAO.class);

    boolean alreadySetup = false;

    @Autowired
    SecurityPrivilegeDAO securityPrivilegeDAO;

    @Autowired
    SecurityProfileDAO securityProfileDAO;

    @Autowired
    ProfileDAO profileDAO;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        LOGGER.info("Checking and creating security privileges");
        SecurityPrivilege MANAGE_PROFILES = securityPrivilegeDAO.updateOrCreateIfNotExists("MANAGE_PROFILES");
        SecurityPrivilege MANAGE_RESIDENCE_GROUPS = securityPrivilegeDAO.updateOrCreateIfNotExists("MANAGE_RESIDENCE_GROUPS");
        SecurityPrivilege MANAGE_RESIDENCES = securityPrivilegeDAO.updateOrCreateIfNotExists("MANAGE_RESIDENCES");
        SecurityPrivilege MANAGE_VISITS = securityPrivilegeDAO.updateOrCreateIfNotExists("MANAGE_VISITS");
        SecurityPrivilege MANAGE_WHITE_LIST = securityPrivilegeDAO.updateOrCreateIfNotExists("MANAGE_WHITE_LIST");

        LOGGER.info("Checking and creating security profiles");
        securityProfileDAO.updateOrCreateIfNotExists("ADMIN", Sets.newHashSet(securityPrivilegeDAO.findAll()));
        securityProfileDAO.updateOrCreateIfNotExists("CONCIERGE", Sets.newHashSet(MANAGE_PROFILES, MANAGE_VISITS));
        securityProfileDAO.updateOrCreateIfNotExists("DWELLER", Sets.newHashSet(MANAGE_WHITE_LIST));

        LOGGER.info("Checking and creating system users");
        profileDAO.createIfNotExists(new Profile("System Admin", "00000000000", "admin", Sets.newHashSet("ADMIN")));
        profileDAO.createIfNotExists(new Profile("Portaria", "11111111111", "portaria", Sets.newHashSet("CONCIERGE")));
        profileDAO.createIfNotExists(new Profile("Morador", "22222222222", "morador", Sets.newHashSet("DWELLER")));

        alreadySetup = true;
    }
}
