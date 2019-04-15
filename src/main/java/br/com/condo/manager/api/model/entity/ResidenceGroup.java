package br.com.condo.manager.api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "residence_group")
@Data
@NoArgsConstructor
public class ResidenceGroup implements Serializable {

    private static final long serialVersionUID = -4108788278133915312L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Integer doors;

    @JsonIgnore
    @OneToMany(mappedBy = "group", orphanRemoval = true)
    private List<Residence> residences;

}
