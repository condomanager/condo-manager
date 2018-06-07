package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Building;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import org.springframework.stereotype.Service;

@Service
public class BuildingDAO extends BaseSpringDataDAO<Building, Long> {

}
