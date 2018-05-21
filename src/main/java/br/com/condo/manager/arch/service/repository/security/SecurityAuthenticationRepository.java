package br.com.condo.manager.arch.service.repository.security;

import br.com.condo.manager.arch.model.entity.security.SecurityAuthentication;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityAuthenticationRepository extends SpringDataRepository<SecurityAuthentication, String> {

}
