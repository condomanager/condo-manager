package br.com.condo.manager.arch.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_credentials")
@Data
@NoArgsConstructor
public class UserCredentials implements Serializable {

    private static final long serialVersionUID = -8093650591362727469L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "expired")
    private boolean expired;

}
