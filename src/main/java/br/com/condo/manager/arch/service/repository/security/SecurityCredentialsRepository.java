package br.com.condo.manager.arch.service.repository.security;

import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityCredentialsRepository extends SpringDataRepository<SecurityCredentials, Long> {

}
