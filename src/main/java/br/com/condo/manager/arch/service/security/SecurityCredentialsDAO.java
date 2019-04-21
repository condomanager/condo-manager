package br.com.condo.manager.arch.service.security;

import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class SecurityCredentialsDAO extends BaseSpringDataDAO<SecurityCredentials, Long> {

    @Value("${app.security.salt}")
    private String securitySalt;

    @Override
    public SecurityCredentials create(SecurityCredentials entity) {
        String encryptedPassword = encryptPassword(entity.getPassword());
        entity.setPassword(encryptedPassword);
        return super.create(entity);
    }

    /**
     * <p>Cria um <i>hash</i> de um password informado.</p>
     *
     * @param originalPassword
     * @return o <i>hash</i> criado a partir do password original
     */
    private String encryptPassword(String originalPassword) {
        //FIXME: implementar uma forma de encriptação mais adequada
        /*
        SecureRandom random = new SecureRandom();
        byte[] salt = securitySalt.getBytes(StandardCharsets.UTF_8);
        random.nextBytes(salt);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(salt);

        byte[] hashedPassword = md.digest(originalPassword.getBytes(StandardCharsets.UTF_8));
        return new String(hashedPassword);
        */
        String seed = originalPassword + "_" + this.securitySalt;
        return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }

    public boolean checkAvailability(String username) {
        Long occurrences = count(new SearchParameter("username", SearchParameter.Operator.EQUAL, username));
        return occurrences.equals(0L);
    }

    public Optional<SecurityCredentials> retrieve(String username) {
        Collection<SecurityCredentials> credentialsCollection = find(new SearchParameter("username", SearchParameter.Operator.EQUAL, username));
        if(credentialsCollection.isEmpty()) return Optional.empty();
        if(credentialsCollection.size() > 1) throw new RuntimeException("There is more than one user for the same username");

        return credentialsCollection.stream().findFirst();
    }

    public Optional<SecurityCredentials> retrieve(String username, String password) {
        String encryptedPassword = encryptPassword(password);
        return retrieve(username).filter(c -> c.getPassword().equals(encryptedPassword));
    }

}
