package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Person;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("people")
public class PersonController extends BaseEndpoint<Person, Long> {
}
