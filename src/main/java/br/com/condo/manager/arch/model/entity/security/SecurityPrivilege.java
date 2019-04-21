package br.com.condo.manager.arch.model.entity.security;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "security_privilege")
@Data
@NoArgsConstructor
public class SecurityPrivilege implements Serializable {

    private static final long serialVersionUID = 7079385729442218234L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "securityPrivileges")
    private Set<SecurityProfile> securityProfiles;

    public SecurityPrivilege(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityPrivilege that = (SecurityPrivilege) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
