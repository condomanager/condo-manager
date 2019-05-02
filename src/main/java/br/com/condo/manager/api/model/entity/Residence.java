package br.com.condo.manager.api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "residence")
@Data
@NoArgsConstructor
public class Residence implements Serializable {

    private static final long serialVersionUID = 1359135403590580087L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @JsonIgnore
    @ManyToOne
    private ResidenceGroup group;

    @JsonIgnore
    @OneToMany(mappedBy = "residence")
    private Set<Profile> profiles;

    @JsonIgnore
    @OneToMany (mappedBy = "residence")
    private List<Visit> visits;

}
