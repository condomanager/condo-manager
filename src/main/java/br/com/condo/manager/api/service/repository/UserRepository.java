package br.com.condo.manager.api.service.repository;

import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import br.com.condo.manager.api.model.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends SpringDataRepository<User, Long> {

}
