package br.com.condo.manager.arch.service.repository;

import br.com.condo.manager.arch.model.entity.Authentication;
import org.springframework.stereotype.Repository;

@Repository
interface AuthenticationRepository extends SpringDataRepository<Authentication, String> {

}
