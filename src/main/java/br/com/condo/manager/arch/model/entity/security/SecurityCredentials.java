package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "security_credentials")
@Data
@NoArgsConstructor
public class SecurityCredentials implements Serializable {

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "security_credentials_profiles",
            joinColumns = @JoinColumn(name = "fk_security_credentials", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "fk_security_profile", referencedColumnName = "id"))
    @Fetch(FetchMode.SUBSELECT)
    private Collection<SecurityProfile> securityProfiles;

}
