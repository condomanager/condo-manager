package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.api.model.entity.Visit;
import br.com.condo.manager.api.model.entity.Visitor;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.api.service.ResidenceDAO;
import br.com.condo.manager.api.service.VisitorDAO;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.controller.exception.NotFoundException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("visits")
public class VisitController extends ResidenceDependentEndpoint<Visit, Long> {

    @Autowired
    ResidenceDAO residenceDAO;
    @Autowired
    VisitorDAO visitorDAO;

    @Autowired
    ProfileDAO profileDAO;

    protected void validateOrCreateVisitor(Visit visit, Profile author) {
        if(visit.getVisitor().getId() != null) {
            Visitor visitor = visitorDAO.retrieve(visit.getVisitor().getId()).get();
            if(visitor == null || !visitor.getResidence().getId().equals(visit.getResidence().getId()))
                throw new NotFoundException("A Visitor of ID " + visit.getVisitor().getId() + " was not found");

            if(visit.getVisitor().getDocument() != null && !visit.getVisitor().getDocument().trim().isEmpty() && visitor.getDocument() == null) {
                visitor.setDocument(visit.getVisitor().getDocument());
                visit.setVisitor(visitorDAO.update(visitor));
            }
        } else {
            if(visit.getVisitor().getName() == null || visit.getVisitor().getName().trim().isEmpty())
                throw new BadRequestException("Invalid data: the name of the Visitor must be informed");

            visit.getVisitor().setResidence(visit.getResidence());
            visit.getVisitor().setAuthor(author);
            Visitor createdVisitor = visitorDAO.create(visit.getVisitor());
            visit.setVisitor(createdVisitor);
        }
    }

    @Override
    protected Visit validateRequestDataForCreate(Visit requestData) {
        SecurityCredentials credentials = securityUtils.authenticatedCredentials();
        Profile author = profileDAO.retrieve(credentials.getId()).get();

        if(requestData.getResidence() == null)
            throw new BadRequestException("Invalid data: the Residence to where the visit is going must be informed");

        Optional<Residence> residence = residenceDAO.retrieve(requestData.getResidence().getId());
        if(!residence.isPresent())
            throw new BadRequestException("Invalid data: a Residence of ID " + requestData.getResidence().getId() + " was not found");

        if(requestData.getVisitor() == null)
            throw new BadRequestException("Invalid data: the Visitor must be informed");

        validateOrCreateVisitor(requestData, author);

        boolean hasLicensePlate = requestData.getLicensePlate() != null && !requestData.getLicensePlate().trim().isEmpty();
        boolean hasDocument = requestData.getVisitor().getDocument() != null && !requestData.getVisitor().getDocument().trim().isEmpty();
        if(!hasLicensePlate && !hasDocument)
            throw new BadRequestException("Invalid data: the license plate of the vehicle or the document of the visitor must be informed for identification");

        requestData.setAuthor(author);
        requestData.setCreateDate(new Date());
        requestData.setAuthorizeDate(requestData.getVisitor().getAuthorizeDate() != null ? requestData.getCreateDate() : null);
        requestData.setDenyDate(null);
        requestData.setDepartureDate(null);
        requestData.setDeleteDate(null);

        return  requestData;
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visit>> find(@RequestParam Map<String, String> requestParams) {
        return super.find(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(@RequestParam Map<String, String> requestParams) {
        return super.count(requestParams);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public ResponseEntity<Visit> create(@RequestBody Visit requestData) {
        return super.create(requestData);
    }

    @Override
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> update(@PathVariable Long id, @RequestBody Visit requestData) {
        throw new ForbiddenException("It is not possible to update a visit. Use '/visit/ID/authorize', '/visit/ID/deny' or '/visit/ID/close'");
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
    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        if(visit.getDepartureDate() != null)
            throw new ForbiddenException("This visit has already been closed and can not be deleted");

        return super.delete(id);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/authorize"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> authorize(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        if(visit.getAuthorizeDate() != null)
            throw new ForbiddenException("This visit has already been authorized");
        if(visit.getDenyDate() != null)
            throw new ForbiddenException("This visit has already been denied and can not be authorized");
        if(visit.getDepartureDate() != null)
            throw new ForbiddenException("This visit has already been closed and can not be authorized");

        visit.setAuthorizeDate(new Date());
        return super.update(id, visit);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/deny"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> deny(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        if(visit.getDenyDate() != null)
            throw new ForbiddenException("This visit has already been denied");
        if(visit.getAuthorizeDate() != null)
            throw new ForbiddenException("This visit has already been authorized and can not be denied");
        if(visit.getDepartureDate() != null)
            throw new ForbiddenException("This visit has already been closed and can not be denied");

        visit.setDenyDate(new Date());
        return super.update(id, visit);
    }

    @PreAuthorize("hasAuthority('MANAGE_VISITS')")
    @PutMapping(value = {"/{id}/close", "/{id}/close/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Visit> close(@PathVariable Long id) {
        Visit visit = retrieveResource(id);
        if(visit.getDepartureDate() != null)
            throw new ForbiddenException("This visit has already been closed");
        if(visit.getAuthorizeDate() == null)
            throw new ForbiddenException("This visit has not been authorized yet and can not be closed");
        if(visit.getDenyDate() != null)
            throw new ForbiddenException("This visit has already been denied and can not be closed");

        visit.setDepartureDate(new Date());
        return super.update(id, visit);
    }

    @GetMapping(value = {"/my-visits"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Visit>> findMyVists() {
        Residence myResidence = retrieveMyResidence();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myResidence.getId().toString());
        return super.find(parameters);
    }

    @GetMapping(value = {"/my-visits/count"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> countMyVists() {
        Residence myResidence = retrieveMyResidence();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("residence.id", myResidence.getId().toString());
        return super.count(parameters);
    }
}
