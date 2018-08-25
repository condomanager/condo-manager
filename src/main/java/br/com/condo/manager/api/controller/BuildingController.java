package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Building;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("buildings")
public class BuildingController extends BaseEndpoint<Building, Long> {

    @PreAuthorize("hasAuthority('PAINT_BUILDINGS')")
    @GetMapping(value = {"/paint"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void paintBuilding() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

    @GetMapping(value = {"/paint/free"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void paintFreeBuilding() {
        SecurityCredentials auth = securityUtils.authenticatedCredentials();
        if (auth != null)
            auth.getSecurityProfiles();
    }

}
