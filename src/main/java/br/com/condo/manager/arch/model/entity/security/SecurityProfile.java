package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "security_profile")
@Data
@NoArgsConstructor
public class SecurityProfile implements Serializable {

    private static final long serialversionuid = 1766412300783000411l;

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
    private Set<SecurityPrivilege> securityPrivileges;

    public SecurityProfile(String name, Set<SecurityPrivilege> securityPrivileges) {
        this.name = name;
        this.securityPrivileges = securityPrivileges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityProfile that = (SecurityProfile) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
