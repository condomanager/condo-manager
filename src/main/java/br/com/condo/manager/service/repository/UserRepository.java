package br.com.condo.manager.service.repository;

import br.com.condo.arch.service.repository.SpringDataRepository;
import br.com.condo.manager.model.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends SpringDataRepository<User, Long> {

}
