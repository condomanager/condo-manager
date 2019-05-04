package br.com.condo.manager.api.controller;

import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.arch.controller.BaseEndpoint;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("residences")
public class ResidenceController extends BaseEndpoint<Residence, Long> {

    @Override
    public ResponseEntity<Residence> create(Residence requestData) {
        throw new ForbiddenException("It is not possible to manage residences this way. You should the Residence Group sub resource endpoint: /residence-groups/{groupId}/residences");
    }

    @Override
    public ResponseEntity<Residence> update(Long id, Residence requestData) {
        throw new ForbiddenException("It is not possible to manage residences this way. You should the Residence Group sub resource endpoint: /residence-groups/{groupId}/residences/{id}");
    }

    @Override
    public ResponseEntity delete(Long id) {
        throw new ForbiddenException("It is not possible to manage residences this way. You should the Residence Group sub resource endpoint: /residence-groups/{groupId}/residences/{id}");
    }
}
