package br.com.condo.manager.arch.controller;

import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.NotFoundException;
import br.com.condo.manager.arch.model.entity.Authentication;
import br.com.condo.manager.arch.model.entity.UserCredentials;
import br.com.condo.manager.arch.service.AuthenticationDAO;
import br.com.condo.manager.arch.service.UserCredentialsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class AuthenticationController {

    @Autowired
    private UserCredentialsDAO userCredentialsDAO;
    @Autowired
    private AuthenticationDAO authenticationDAO;

    @PostMapping(value = {"/login"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        if(username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty())
            throw new BadRequestException("Invalid credentials: both username and password must not be empty");

        UserCredentials credentials = userCredentialsDAO.retrieve(username, password)
                .orElseThrow(() -> new NotFoundException("Invalid credentials: no user was found"));
        if(!credentials.isEnabled())
            throw new BadRequestException("Invalid credentials: account is not enabled and can not be used");
        if(credentials.isLocked())
            throw new BadRequestException("Invalid credentials: account is locked and can not be used");
        if(credentials.isExpired())
            throw new BadRequestException("Invalid credentials: account is expired and can not be used");

        Authentication authentication = authenticationDAO.retrieve(credentials).orElseGet(() -> authenticationDAO.create(new Authentication(credentials)));

        Map<String, String> body = new HashMap<>();
        body.put("authentication", authentication.getId());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

}
