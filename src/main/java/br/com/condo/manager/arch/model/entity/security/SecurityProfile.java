package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "security_profile")
@Data
@NoArgsConstructor
public class SecurityProfile implements Serializable {

    private static final long serialVersionUID = 1766412300783000411L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "security_profile_privileges",
            joinColumns = @JoinColumn(name = "security_profile_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "security_privilege_id", referencedColumnName = "id"))
    @Fetch(FetchMode.SUBSELECT)
    private Collection<SecurityPrivilege> securityPrivileges;

    public SecurityProfile(String name, Collection<SecurityPrivilege> securityPrivileges) {
        this.name = name;
        this.securityPrivileges = securityPrivileges;
    }
}
