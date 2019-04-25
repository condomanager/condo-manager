package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.ResidenceGroup;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.api.service.ResidenceGroupDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.NotFoundException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("residence-groups")
public class ResidenceGroupController extends BaseEndpoint<ResidenceGroup, Long> {

    @Autowired
    private ResidenceDAO residenceDAO;

    private Residence validateAndRetrieveResidence(Long groupId, Long id) {
        ResidenceGroup residenceGroup = retrieveResource(groupId);

        Optional<Residence> retrieveResult = residenceDAO.retrieve(id);
        if (!retrieveResult.isPresent()) throw new NotFoundException("Residence of ID " + id + " not found");

        Residence residence = retrieveResult.get();
        if (!residence.getGroup().getId().equals(residenceGroup.getId())) throw new BadRequestException("Residence of ID " + id + " does not belong to the ResidenceGroup of ID " + residenceGroup.getId());

        return residence;
    }

    private void validateResidence(Residence residence) {
        if (residence.getName() == null || residence.getName().isEmpty())
            throw new BadRequestException("Invalid data: residence name is required");
    }

    private void validateResidenceGroup(ResidenceGroup residenceGroup) {
        if (residenceGroup.getName() == null || residenceGroup.getName().isEmpty())
            throw new BadRequestException("Invalid data: residence group name is required");
    }

    @Override
    protected ResidenceGroup validateRequestDataForCreate(ResidenceGroup requestData) {
        validateResidenceGroup(requestData);
        return requestData;
    }

    @Override
    protected ResidenceGroup validateRequestDataForUpdate(ResidenceGroup requestData, ResidenceGroup currentData) {
        currentData.getResidences().clear();
        if (requestData.getResidences() != null) {
            currentData.getResidences().addAll(requestData.getResidences());
            requestData.setResidences(currentData.getResidences());
        }

        validateResidenceGroup(requestData);
        return requestData;
    }

    // =================================================================================================================
    // OVERRIDES
    // =================================================================================================================


    @PreAuthorize("hasAuthority('MANAGE_RESIDENCE_GROUPS')")
    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ResidenceGroup> create(@RequestBody ResidenceGroup requestData) {
        return super.create(requestData);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCE_GROUPS')")
    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable Long id) {
        return super.exists(id);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCE_GROUPS')")
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ResidenceGroup> update(@PathVariable Long id, @RequestBody ResidenceGroup requestData) {
        return super.update(id, requestData);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCE_GROUPS')")
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
        return super.delete(id);
    }


    // =================================================================================================================
    // ENDPOINTS DO SUB-RECURSO "RESIDENCES"
    // =================================================================================================================


    @GetMapping(value = {"{groupId}/residences", "{groupId}/residences/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Residence>> findResidences(@PathVariable Long groupId, @RequestParam Map<String,String> requestParams) {
        ResidenceGroup residenceGroup = retrieveResource(groupId);
        requestParams.put("group.id", residenceGroup.getId().toString());
        Collection<Residence> result = (Collection<Residence>)executeFind(requestParams, residenceDAO, Residence.class);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = {"{groupId}/residences/count", "{groupId}/residences/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countResidences(@PathVariable Long groupId, @RequestParam Map<String,String> requestParams) {
        ResidenceGroup residenceGroup = retrieveResource(groupId);
        requestParams.put("group.id", residenceGroup.getId().toString());
        Long result = residenceDAO.count(getSearchParameters(Residence.class, requestParams));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCES')")
    @PostMapping(value = {"{groupId}/residences", "{groupId}/residences/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Residence> createResidence(@PathVariable Long groupId, @RequestBody Residence requestData) {
        ResidenceGroup residenceGroup = retrieveResource(groupId);
        requestData.setGroup(residenceGroup);
        validateResidence(requestData);
        Residence residence = residenceDAO.create(requestData);
        return new ResponseEntity<>(residence, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCES')")
    @PutMapping(value = {"{groupId}/residences/{id}", "{groupId}/residences/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Residence> updateResidence(@PathVariable Long groupId, @PathVariable Long id, @RequestBody Residence requestData) {
        Residence retrieveResult = validateAndRetrieveResidence(groupId, id);
        validateResidence(requestData);
        requestData.setId(retrieveResult.getId());
        Residence residence = residenceDAO.update(requestData);
        return new ResponseEntity<>(residence, HttpStatus.OK);
    }

    @GetMapping(value = {"{groupId}/residences/{id}", "{groupId}/residences/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Residence> retrieveResidence(@PathVariable Long groupId, @PathVariable Long id) {
        Residence retrieveResult = validateAndRetrieveResidence(groupId, id);
        return new ResponseEntity<>(retrieveResult, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('MANAGE_RESIDENCES')")
    @DeleteMapping(value = {"{groupId}/residences/{id}", "{groupId}/residences/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Residence> deleteResidence(@PathVariable Long groupId, @PathVariable Long id, @RequestBody Residence requestData) {
        Residence retrieveResult = validateAndRetrieveResidence(groupId, id);
        residenceDAO.delete(retrieveResult);
        return new ResponseEntity(HttpStatus.OK);
    }

}
