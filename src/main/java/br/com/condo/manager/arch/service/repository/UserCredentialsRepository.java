package br.com.condo.manager.arch.service.repository;

import br.com.condo.manager.arch.model.entity.UserCredentials;
import org.springframework.stereotype.Repository;

@Repository
interface UserCredentialsRepository extends SpringDataRepository<UserCredentials, Long> {

}
