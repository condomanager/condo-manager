package br.com.condo.manager.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "profile")
@Data
@NoArgsConstructor
public class Profile implements Serializable {

    private static final long serialVersionUID = -780346953789267213L;

    @Id
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column
    private String name;

    @Column
    private Date birthday;

    @Column
    private String cpf;

    @Column
    private String email;

    @OneToMany(mappedBy = "profile", fetch = FetchType.EAGER , cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    private List<Phone> phones;

    @Transient
    private String username;

    @Transient
    private String password;

    @Transient
    private Set<String> securityProfiles;

    public Profile(String name, String username, String password, Set<String> securityProfiles) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.securityProfiles = securityProfiles;
    }

}
