package br.com.condo.manager.controller;

import br.com.condo.arch.controller.BaseEndpoint;
import br.com.condo.manager.model.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController extends BaseEndpoint<User, Long> {

}
