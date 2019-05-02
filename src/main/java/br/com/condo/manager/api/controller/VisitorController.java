package br.com.condo.manager.api.controller;


import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.Visit;
import br.com.condo.manager.api.model.entity.Visitor;
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
@RequestMapping("visitors")
public class VisitorController extends ResidenceDependentEndpoint<Visitor, Long> {

    @Autowired
    ResidenceDAO residenceDAO;

    @Autowired
    ProfileDAO profileDAO;

    @Override
    protected Visitor validateRequestDataForCreate(Visitor requestData) {
        Optional<Residence> residence = residenceDAO.retrieve(requestData.getResidence().getId());
        if(!residence.isPresent())
            throw new BadRequestException("Invalid data: a Residence of ID " + requestData.getResidence().getId() + " was not found");

        if(requestData.getName() == null || requestData.getName().trim().isEmpty())
            throw new BadRequestException("Invalid data: the name of the visitor must be informed");

        return  requestData;
    }

    @Override
    protected Visitor validateRequestDataForUpdate(Visitor requestData, Visitor currentData) {
        this.validateRequestDataForCreate(requestData);
        requestData.setCreationDate(currentData.getCreationDate());
        requestData.setResidence(currentData.getResidence());
        return requestData;
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visitor>> find(Map<String, String> requestParams) {
        return super.find(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(Map<String, String> requestParams) {
        return super.count(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> create(@RequestBody Visitor requestData) {
        return super.create(requestData);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> update(@PathVariable Long id, @RequestBody Visitor requestData) {
        Visitor currentData = retrieveResource(id);
        requestData.setAuthorizeDate(currentData.getAuthorizeDate());
        return super.update(id, requestData);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable Long id) {
        return super.exists(id);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @GetMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> retrieve(@PathVariable Long id) {
        return super.retrieve(id);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITORS')")
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
       return  super.delete(id);
    }

    @GetMapping(value = {"/my-visitors"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visitor>> findMyVisitors() {
        Residence myResidence = retrieveMyResidence();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myResidence.getId().toString());
        return super.find(parameters);
    }

    @GetMapping(value = {"/my-visitors/count"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countMyVisitors() {
        Residence myResidence = retrieveMyResidence();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myResidence.getId().toString());
        return super.count(parameters);
    }

    @PostMapping(value = {"/my-visitors"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> createMyVisitor(@RequestBody Visitor requestData) {
        Residence myResidence = retrieveMyResidence();
        requestData.setResidence(myResidence);
        requestData.setAuthorizeDate(new Date());
        return super.create(requestData);
    }

    @PutMapping(value = {"/my-visitors/{id}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> updateMyVisitor(@PathVariable Long id, @RequestBody Visitor requestData) {
        Residence myResidence = retrieveMyResidence();
        Visitor currentData = retrieveResource(id);
        if(requestData.getAuthorizeDate() != null && currentData.getAuthorizeDate() != null && !requestData.getAuthorizeDate().equals(currentData.getAuthorizeDate()))
            requestData.setAuthorizeDate(currentData.getAuthorizeDate());
        requestData.setResidence(myResidence);
        return super.update(id, requestData);
    }

    @DeleteMapping(value = {"/my-visitors/{id}", "/my-visitors/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteMyVisitor(@PathVariable Long id) {
        Residence myResidence = retrieveMyResidence();
        Visitor visitor = retrieveResource(id);
        if(myResidence.equals(visitor.getResidence())){
            return super.delete(id);
        } throw new ForbiddenException("You can't delete a visitor from another residence");
    }

}
