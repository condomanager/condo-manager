package br.com.condo.manager.api.graphql;

import br.com.condo.manager.api.model.entity.User;
import br.com.condo.manager.api.service.UserDAO;
import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Query implements GraphQLQueryResolver {
    @Autowired
    private UserDAO userDAO;

    public Optional<User> getUserById(Long id){
        return this.userDAO.retrieve(id);
    }
}
