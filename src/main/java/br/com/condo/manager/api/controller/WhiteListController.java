package br.com.condo.manager.api.controller;

/**
 *
 */

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.Visit;
import br.com.condo.manager.api.model.entity.WhiteList;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("whitelists")
public class WhiteListController extends BaseEndpoint<WhiteList, Long> {

    @Autowired
    ResidenceDAO residenceDAO;

    @Autowired
    ProfileDAO profileDAO;

    @Override
    protected WhiteList validateRequestDataForCreate(WhiteList requestData) {

        Optional<Residence> residence = residenceDAO.retrieve(requestData.getResidence().getId());
        if(!residence.isPresent())
            throw new BadRequestException("Invalid data: a Residence of ID " + requestData.getResidence().getId() + " was not found");

        if(requestData.getName() == null || requestData.getName().trim().isEmpty())
            throw new BadRequestException("Invalid data: the name of the visitor must be informed");

        requestData.setAuthorizeDate(new Date());
        requestData.setCancelDate(null);

        return  requestData;
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<WhiteList>> find(Map<String, String> requestParams) {
        return super.find(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(Map<String, String> requestParams) {
        return super.count(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<WhiteList> create(@RequestBody WhiteList requestData) {
        requestData.setAuthorizeDate(new Date());
        return super.create(requestData);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<WhiteList> update(@PathVariable Long id, @RequestBody WhiteList requestData) {
        requestData.setAuthorizeDate(new Date());
        return super.update(id, requestData);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable Long id) {
        return super.exists(id);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @GetMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<WhiteList> retrieve(@PathVariable Long id) {
        return super.retrieve(id);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_WHITE_LIST')")
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
       WhiteList whiteListed = retrieveResource(id);
       whiteListed.setCancelDate(new Date());
       return  super.update(id, whiteListed);
    }


    @GetMapping(value = {"/my-white-lists"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<WhiteList>> findMyWhiteLists() {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if(myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myProfile.getResidence().getId().toString());
        return super.find(parameters);
    }

    @GetMapping(value = {"/my-white-lists/count"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countMyWhiteLists() {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if(myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myProfile.getResidence().getId().toString());
        return super.count(parameters);
    }

    @PostMapping(value = {"/my-white-lists"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<WhiteList> createMyWhiteLists(@RequestBody WhiteList requestData) {

        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if (myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Residence residence = myProfile.getResidence();
        requestData.setResidence(residence);
        requestData.setAuthorizeDate(new Date());
        return super.create(requestData);
    }

    @DeleteMapping(value = {"/my-white-lists/{id}", "/my-white-lists/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteMyWhiteList(@PathVariable Long id) {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if (myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Residence residence = myProfile.getResidence();

        WhiteList whiteListed = retrieveResource(id);
        Residence home = whiteListed.getResidence();

        if(residence.equals(home)){
            whiteListed.setCancelDate(new Date());
            return  super.update(id, whiteListed);
        } throw new ForbiddenException("You can't delete a whitelisted from another residence");

    }

}
