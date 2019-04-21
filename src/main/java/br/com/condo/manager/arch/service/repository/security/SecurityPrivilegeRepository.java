package br.com.condo.manager.arch.service.repository.security;

import br.com.condo.manager.arch.model.entity.security.SecurityPrivilege;
import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityPrivilegeRepository extends SpringDataRepository<SecurityPrivilege, Long> {

}
