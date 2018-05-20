package br.com.condo.manager.api.graphql;

import br.com.condo.manager.api.model.entity.User;
import br.com.condo.manager.api.service.UserDAO;
import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Mutation implements GraphQLMutationResolver {
    @Autowired
    private UserDAO userDAO;

    public User saveUser(User user) {
        return this.userDAO.create(user);
    }
}
