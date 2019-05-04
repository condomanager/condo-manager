package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class ResidenceDependentEndpoint<E extends Serializable, P extends Serializable> extends BaseEndpoint<E, P> {

    @Autowired
    protected ResidenceDAO residenceDAO;

    @Autowired
    protected ProfileDAO profileDAO;

    protected Residence retrieveMyResidence() {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if(myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        return myProfile.getResidence();
    }
}
