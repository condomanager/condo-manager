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
    @JoinColumn(name = "security_credentials_id")
    private SecurityCredentials securityCredentials;

    @Column(name = "login_date")
    private Date loginDate;

    @Column(name = "logout_date")
    private Date logoutDate;

    public SecurityAuthentication(SecurityCredentials securityCredentials) {
        this.securityCredentials = securityCredentials;
        this.loginDate = new Date();
        this.id = this.generateId();
    }

    public String generateId() {
        //TODO: modificar o seed para que leve em conta a data + username + password
        /*StringBuilder seed = new StringBuilder()
                .append(loginDate)
                .append(this.securityCredentials.getUsername())
                .append(this.securityCredentials.getPassword());*/
        StringBuilder seed = new StringBuilder()
                .append(this.securityCredentials.getId())
                .append("_")
                .append(loginDate.getTime());
        return UUID.nameUUIDFromBytes(seed.toString().getBytes()).toString();
    }

}
