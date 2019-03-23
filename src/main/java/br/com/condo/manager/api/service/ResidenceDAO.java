package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Residence;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import org.springframework.stereotype.Service;

@Service
public class ResidenceDAO extends BaseSpringDataDAO<Residence, Long> {
}
