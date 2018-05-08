package br.com.condo.manager.arch.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "authentication")
@Data
@NoArgsConstructor
public class Authentication implements Serializable {

    private static final long serialVersionUID = -2483802044062703779L;

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user_credentials")
    private UserCredentials userCredentials;

    @Column(name = "login_date")
    private Date loginDate;

    @Column(name = "logout_date")
    private Date logoutDate;

    public Authentication(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
        this.loginDate = new Date();

        String seed = this.userCredentials.getId().toString() + "_" + String.valueOf(loginDate.getTime());
        this.id = UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }

}
