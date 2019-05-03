package br.com.condo.manager.api.controller;


import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.Visitor;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    private Visitor validateAndRetrieveMyVisitor(Long id) {
        Residence myResidence = retrieveMyResidence();
        Visitor visitor = retrieveResource(id);
        if(!visitor.getResidence().equals(myResidence))
            throw new NotFoundException("A visitor of ID " + id + " was not found for your residence");
        return visitor;
    }

    protected Visitor validateRequestDataForPersistence(Visitor requestData) {
        if(requestData.getName() == null || requestData.getName().trim().isEmpty())
            throw new BadRequestException("Invalid data: the name of the visitor must be informed");

        return  requestData;
    }

    @Override
    protected Visitor validateRequestDataForCreate(Visitor requestData) {
        Optional<Residence> residence = residenceDAO.retrieve(requestData.getResidence().getId());
        if(!residence.isPresent())
            throw new BadRequestException("Invalid data: a Residence of ID " + requestData.getResidence().getId() + " was not found");

        validateRequestDataForPersistence(requestData);
        return  requestData;
    }

    @Override
    protected Visitor validateRequestDataForUpdate(Visitor requestData, Visitor currentData) {
        validateRequestDataForPersistence(requestData);
        requestData.setResidence(currentData.getResidence());
        requestData.setCreationDate(currentData.getCreationDate());
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

    @GetMapping(value = {"/my-visitors/{id}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> retrieveMyVisitor(@PathVariable Long id) {
        Visitor retrieveResult = validateAndRetrieveMyVisitor(id);
        return  new ResponseEntity<>(retrieveResult, HttpStatus.OK);
    }

    @PutMapping(value = {"/my-visitors/{id}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visitor> updateMyVisitor(@PathVariable Long id, @RequestBody Visitor requestData) {
        Visitor currentData = validateAndRetrieveMyVisitor(id);
        if(requestData.getAuthorizeDate() != null && currentData.getAuthorizeDate() != null && !requestData.getAuthorizeDate().equals(currentData.getAuthorizeDate()))
            requestData.setAuthorizeDate(currentData.getAuthorizeDate());
        return super.update(id, requestData);
    }

    @DeleteMapping(value = {"/my-visitors/{id}", "/my-visitors/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteMyVisitor(@PathVariable Long id) {
        Visitor retrieveResult = validateAndRetrieveMyVisitor(id);
        return super.delete(id);
    }

}
