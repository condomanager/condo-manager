package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.Visitor;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisitorDAO extends BaseSpringDataDAO<Visitor, Long> {

    @Override
    public void delete(Visitor entity) {
        entity.setDeleteDate(new Date());
        super.update(entity);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Visitor> retrieveResult = retrieve(id);
        if(retrieveResult.isPresent())
            delete(retrieveResult.get());
    }

    @Override
    public void deleteById(Collection<Long> ids) {
        ids.stream().forEach(id -> deleteById(id));
    }

    @Override
    public void delete(Collection<Visitor> entities) {
        entities.stream().forEach(entity -> delete(entity));
    }

    @Override
    protected Collection<SearchParameter> defaultSearchParameters() {
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(new SearchParameter("deleteDate", SearchParameter.Operator.IS_NULL));
        return searchParameters;
    }

    @Override
    public Visitor create(Visitor entity) {
        entity.setCreationDate(new Date());
        entity.setDeleteDate(null);
        return super.create(entity);
    }
}
