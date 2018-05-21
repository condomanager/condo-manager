package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "security_authentication")
@Data
@NoArgsConstructor
public class SecurityAuthentication implements Serializable {

    private static final long serialVersionUID = -2483802044062703779L;

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_security_credentials")
    private SecurityCredentials securityCredentials;

    @Column(name = "login_date")
    private Date loginDate;

    @Column(name = "logout_date")
    private Date logoutDate;

    public SecurityAuthentication(SecurityCredentials securityCredentials) {
        this.securityCredentials = securityCredentials;
        this.loginDate = new Date();

        String seed = this.securityCredentials.getId().toString() + "_" + String.valueOf(loginDate.getTime());
        this.id = UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }

}
