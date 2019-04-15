package br.com.condo.manager.api.service;

import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.api.model.entity.Phone;
import org.springframework.stereotype.Service;

@Service
public class PhoneDAO extends BaseSpringDataDAO<Phone, Long> {
}
