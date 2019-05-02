package br.com.condo.manager.api.service;

import br.com.condo.manager.api.model.entity.WhiteList;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WhiteListDAO extends BaseSpringDataDAO<WhiteList, Long> {

   /* @Override
    public void delete(Visit entity) {
        entity.setCancelDate(new Date());
        super.update(entity);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Visit> retrieveResult = retrieve(id);
        if(retrieveResult.isPresent())
            delete(retrieveResult.get());
    }

    @Override
    public void deleteById(Collection<Long> ids) {
        ids.stream().forEach(id -> deleteById(id));
    }

    @Override
    public void delete(Collection<Visit> entities) {
        entities.stream().forEach(entity -> delete(entity));
    }*/

    @Override
    protected Collection<SearchParameter> defaultSearchParameters() {
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(new SearchParameter("cancelDate", SearchParameter.Operator.IS_NULL));
        return searchParameters;
    }
}
