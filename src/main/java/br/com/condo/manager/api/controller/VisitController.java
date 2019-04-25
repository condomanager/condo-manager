package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.Visit;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("visits")
public class VisitController extends BaseEndpoint<Visit, Long> {

    @Autowired
    ResidenceDAO residenceDAO;

    @Autowired
    ProfileDAO profileDAO;

    @Override
    protected Visit validateRequestDataForCreate(Visit requestData) {
        if(requestData.getResidence() == null)
            throw new BadRequestException("Invalid data: the Residence to where the visit is going must be informed");

        Optional<Residence> residence = residenceDAO.retrieve(requestData.getResidence().getId());
        if(!residence.isPresent())
            throw new BadRequestException("Invalid data: a Residence of ID " + requestData.getResidence().getId() + " was not found");

        if(requestData.getName() == null || requestData.getName().trim().isEmpty())
            throw new BadRequestException("Invalid data: the name of the visitor must be informed");

        boolean hasLicensePlate = requestData.getLicensePlate() != null && !requestData.getLicensePlate().trim().isEmpty();
        boolean hasCpf = requestData.getDocument() != null && !requestData.getDocument().trim().isEmpty();
        if(!hasLicensePlate && !hasCpf)
            throw new BadRequestException("Invalid data: a license plate or a document should be informed for identification");

        requestData.setAuthorizeDate(null);
        requestData.setDenyDate(null);
        requestData.setDepartureDate(null);
        requestData.setCancelDate(null);

        return  requestData;
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visit>> find(Map<String, String> requestParams) {
        return super.find(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(Map<String, String> requestParams) {
        return super.count(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> create(@RequestBody Visit requestData) {
        return super.create(requestData);
    }

    @Override
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> update(@PathVariable Long id, @RequestBody Visit requestData) {
        throw new ForbiddenException("It is not possible to update a visit. Use /visit/ID/close ");
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable Long id) {
        return super.exists(id);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> retrieve(@PathVariable Long id) {
        return super.retrieve(id);
    }

    @Override
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
        throw new ForbiddenException("It is not possible to delete a visit. Use /visit/ID/cancel and inform the reason for this action");
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/authorize"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> authorize(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        visit.setAuthorizeDate(new Date());
        return super.update(id, visit);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/deny"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> deny(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        visit.setDenyDate(new Date());
        return super.update(id, visit);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/cancel", "/{id}/cancel/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> cancel(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        if(visit.getDepartureDate() != null)
            throw new ForbiddenException("It is not possible to cancel an already departed visit");

        visit.setCancelDate(new Date());
        return super.update(id, visit);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/close", "/{id}/close/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> close(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        visit.setDepartureDate(new Date());
        return super.update(id, visit);
    }

    @GetMapping(value = {"/my-visits"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visit>> findMyVists() {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if(myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myProfile.getResidence().getId().toString());
        return super.find(parameters);
    }

    @GetMapping(value = {"/my-visits/count"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countMyVists() {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile myProfile = profileDAO.retrieve(credentials.getId()).get();
        if(myProfile.getResidence() == null)
            throw new ForbiddenException("You must inform your residence in your profile first");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myProfile.getResidence().getId().toString());
        return super.count(parameters);
    }
}
