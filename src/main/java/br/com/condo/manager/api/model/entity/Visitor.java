package br.com.condo.manager.api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "visitor")
@Data
@NoArgsConstructor
public class Visitor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String document;

    @Column
    private Date authorizeDate;

    @Column
    private Date deleteDate;

    @Column
    private Date creationDate;

    @Column
    private String observation;

    @ManyToOne
    private Residence residence;

    @JsonIgnore
    @OneToMany(mappedBy = "visitor")
    private List<Visit> visit;

}
