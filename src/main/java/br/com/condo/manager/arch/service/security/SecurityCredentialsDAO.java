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
    public SecurityCredentials persist(SecurityCredentials entity) {
        if (!isEncryptedPassword(entity.getPassword())) {
            String encryptedPassword = encryptPassword(entity.getPassword());
            entity.setPassword(encryptedPassword);
        }
        return super.persist(entity);
    }

    /**
     * <p>Informa se um determinado password contém as carcterísticas de um valor criptografado.</p>
     *
     * @param password
     * @return <code>true</code> caso considere-se que já está criptografado, <code>false</code> do contrário
     */
    private boolean isEncryptedPassword(String password) {
        //EXEMPLO: 9e6b6e2d-539a-303a-bebb-6f90918850ac
        if (password.contains("-")) {
            String[] passwordBits = password.split("-");
            if (passwordBits.length == 5 &&
                passwordBits[0].length() == 8 &&
                passwordBits[1].length() == 4 &&
                passwordBits[2].length() == 4 &&
                passwordBits[3].length() == 4 &&
                passwordBits[4].length() == 12
            )
                return true;
        }
        return false;
    }

    /**
     * <p>Cria um <i>hash</i> de um password informado.</p>
     *
     * @param originalPassword
     * @return o <i>hash</i> criado a partir do password original
     */
    private String encryptPassword(String originalPassword) {
        //FIXME: implementar uma forma de encriptação mais adequada
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
