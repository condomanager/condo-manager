package br.com.condo.manager.api.service.repository;

import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidenceRepository extends SpringDataRepository<Residence, Long> {
}
