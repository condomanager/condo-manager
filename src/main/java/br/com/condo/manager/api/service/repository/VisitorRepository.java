package br.com.condo.manager.api.service.repository;

import br.com.condo.manager.api.model.entity.Visitor;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorRepository extends SpringDataRepository<Visitor, Long> {

}
