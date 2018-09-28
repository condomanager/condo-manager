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

    @GetMapping(value = {"{id}/residences"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Residence>> findResidences(@PathVariable Long id, @RequestParam Map<String,String> requestParams) {
        ResidenceGroup residenceGroup = retrieveResource(id);
        requestParams.put("group.id", residenceGroup.getId().toString());
        Collection<Residence> result = (Collection<Residence>)executeFind(requestParams, residenceDAO, Residence.class);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = {"{id}/residences/count"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countResidences(@PathVariable Long id, @RequestParam Map<String,String> requestParams) {
        ResidenceGroup residenceGroup = retrieveResource(id);
        requestParams.put("group.id", residenceGroup.getId().toString());
        Long result = residenceDAO.count(getSearchParameters(Residence.class, requestParams));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
