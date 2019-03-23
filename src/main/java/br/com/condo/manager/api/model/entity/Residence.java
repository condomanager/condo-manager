package br.com.condo.manager.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

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
    private String number;

    @Column
    private String obs;

    @ManyToOne
    private ResidenceGroup group;

}
