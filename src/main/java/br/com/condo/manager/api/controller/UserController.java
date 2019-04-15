package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Phone;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController extends BaseEndpoint<Phone, Long> {

    @PreAuthorize("hasRole('DO_NOTHING')")
    @GetMapping(value = {"/nothing/role"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doNothingRole() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasRole('ALSO_DO_NOTHING')")
    @GetMapping(value = {"/also/nothing/role"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void alsoDoNothingRole() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasAuthority('DO_NOTHING')")
    @GetMapping(value = {"/nothing/authority"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doNothingAuthority() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasAuthority('ALSO_DO_NOTHING')")
    @GetMapping(value = {"/also/nothing/authority"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void alsoDoNothingAuthority() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

}
