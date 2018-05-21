package br.com.condo.manager.api.controller;

import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.api.model.entity.User;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.security.SecurityUtils;
import br.com.condo.manager.arch.service.security.SecurityCredentialsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("user")
public class UserController extends BaseEndpoint<User, Long> {

    @PreAuthorize("hasRole('DO_NOTHING')")
    @GetMapping(value = {"/nothing/role"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doNothingRole() {
        SecurityCredentials auth = SecurityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasRole('ALSO_DO_NOTHING')")
    @GetMapping(value = {"/also/nothing/role"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void alsoDoNothingRole() {
        SecurityCredentials auth = SecurityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasAuthority('DO_NOTHING')")
    @GetMapping(value = {"/nothing/authority"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doNothingAuthority() {
        SecurityCredentials auth = SecurityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @PreAuthorize("hasAuthority('ALSO_DO_NOTHING')")
    @GetMapping(value = {"/also/nothing/authority"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void alsoDoNothingAuthority() {
        SecurityCredentials auth = SecurityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

}
