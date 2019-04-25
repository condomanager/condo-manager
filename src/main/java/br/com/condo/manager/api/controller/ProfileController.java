package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Profile;
import br.com.condo.manager.api.service.ProfileDAO;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import com.google.common.collect.Sets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("profiles")
public class ProfileController extends BaseEndpoint<Profile, Long> {

    private void validateUsername(Profile profile) {
        if(profile.getCpf() == null || profile.getCpf().trim().isEmpty())
            throw new BadRequestException("Invalid credentials: CPF is required");

        if(!((ProfileDAO) dao).checkAvailability(profile.getCpf()))
            throw new BadRequestException("Invalid credentials: a profile with this CPF is already registered");
    }

    private void associateProfileToPhones(Profile profile) {
        if(profile.getPhones() != null && !profile.getPhones().isEmpty())
            profile.getPhones().stream().forEach(phone -> phone.setProfile(profile));
    }

    @Override
    protected Profile validateRequestDataForCreate(Profile requestData) {
        if(requestData.getPassword() == null || requestData.getPassword().trim().isEmpty())
            throw new BadRequestException("Invalid credentials: a password is required");

        validateUsername(requestData);
        associateProfileToPhones(requestData);
        return requestData;
    }

    @Override
    protected Profile validateRequestDataForUpdate(Profile requestData, Profile currentData) {
        if (!requestData.getCpf().equals(currentData.getCpf()))
            validateUsername(requestData);

        associateProfileToPhones(requestData);
        return requestData;
    }


    // =================================================================================================================
    // OVERRIDES
    // =================================================================================================================


    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<Profile>> find(@RequestParam Map<String,String> requestParams) {
        return super.find(requestParams);
    }

    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(@RequestParam Map<String,String> requestParams) {
        return super.count(requestParams);
    }

    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> create(@RequestBody Profile requestData) {
        requestData.setSecurityProfiles(Sets.newHashSet("DWELLER"));
        return super.create(requestData);
    }

    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable Long id) {
        return super.exists(id);
    }

    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @GetMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> retrieve(@PathVariable Long id) {
        return super.retrieve(id);
    }

    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> update(@PathVariable Long id, @RequestBody Profile requestData) {
        return super.update(id, requestData);
    }

    @PreAuthorize("hasAuthority('MANAGE_PROFILES')")
    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
        return super.delete(id);
    }


    // =================================================================================================================
    // ENDPOINTS
    // =================================================================================================================


    @GetMapping(value = {"/my-profile", "/my-profile/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> retrieveMyProfile() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        return super.retrieve(auth.getId());
    }

    @PutMapping(value = {"/my-profile", "/my-profile/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> updateMyProfile(@RequestBody Profile requestData) {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        return super.update(auth.getId(), requestData);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = {"/admin", "/admin/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> createAdmin(@RequestBody Profile requestData) {
        requestData.setSecurityProfiles(Sets.newHashSet("ADMIN"));
        return super.create(requestData);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = {"/concierge", "/concierge/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Profile> createConcierge(@RequestBody Profile requestData) {
        requestData.setSecurityProfiles(Sets.newHashSet("CONCIERGE"));
        return super.create(requestData);
    }
}
