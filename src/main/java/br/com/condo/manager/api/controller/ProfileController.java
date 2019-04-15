package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Phone;
import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profiles")
public class ProfileController extends BaseEndpoint<Profile, Long> {
    private ProfileDAO profileDAO = ((ProfileDAO) dao);

    private void associateProfileToPhones(Profile profile) {
        if(profile.getPhones() != null && !profile.getPhones().isEmpty()) {
            for (Phone phone : profile.getPhones()) {
                phone.setProfile(profile);
            }
        }
    }

    @Override
    protected Profile validateRequestDataForCreate(Profile requestData) {
        ProfileDAO profileDAO = (ProfileDAO) dao;

        if(!profileDAO.checkAvailability(requestData.getUsername()))
            throw new BadRequestException("Invalid credentials: username is already in use");

        SecurityCredentials securityCredentials = profileDAO.createSecurityCredentials(requestData.getUsername(), requestData.getPassword());
        requestData.setId(securityCredentials.getId());

        associateProfileToPhones(requestData);
        return requestData;
    }

    @Override
    protected Profile validateRequestDataForUpdate(Profile requestData, Profile currentData) {
        associateProfileToPhones(requestData);
        return requestData;
    }

    //@PreAuthorize("hasRole('DO_NOTHING')")
    @GetMapping(value = {"/my_profile", "/my_profile/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> getMyProfile() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        return retrieve(auth.getId());
    }

}
