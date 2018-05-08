package br.com.condo.manager.api.controller;

import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.api.model.entity.User;
import br.com.condo.manager.arch.model.entity.UserCredentials;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController extends BaseEndpoint<User, Long> {

    @GetMapping(value = {"/nothing"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doNothing(@AuthenticationPrincipal final org.springframework.security.core.userdetails.User credentials) {
        if(credentials != null) {
            credentials.getAuthorities();
        }
    }

}
