package br.com.condo.manager.arch.service.repository;

import br.com.condo.manager.arch.model.entity.EndpointAudit;
import org.springframework.stereotype.Repository;

@Repository
interface EndpointAuditRepository extends SpringDataRepository<EndpointAudit, Long> {

}
