package br.com.condo.manager.service;

import br.com.condo.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserDAO extends BaseSpringDataDAO<User, Long> {

}
