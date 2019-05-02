package br.com.condo.manager.api.service.repository;

import br.com.condo.manager.api.model.entity.Visit;
import br.com.condo.manager.api.model.entity.WhiteList;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhiteListRepository extends SpringDataRepository<WhiteList, Long> {

}
