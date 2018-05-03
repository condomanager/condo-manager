package br.com.condo.manager.api.service;

import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.api.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserDAO extends BaseSpringDataDAO<User, Long> {

}
