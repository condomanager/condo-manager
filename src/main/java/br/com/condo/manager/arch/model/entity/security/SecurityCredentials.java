package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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
    private boolean enabled = false;

    @Column(name = "locked")
    private boolean locked = false;

    @Column(name = "expired")
    private boolean expired = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "security_credentials_profiles",
            joinColumns = @JoinColumn(name = "security_credentials_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "security_profile_id", referencedColumnName = "id"))
    @Fetch(FetchMode.SUBSELECT)
    private Set<SecurityProfile> securityProfiles;

    public SecurityCredentials(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public SecurityCredentials(String username, String password) {
        this(null, username, password);
    }

}
