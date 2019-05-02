package br.com.condo.manager.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "visit")
@Data
@NoArgsConstructor
public class Visit implements Serializable {

    private static final long serialVersionUID = 1359135403590580087L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String licensePlate;

    @Column
    private String document;

    @Column
    private Date authorizeDate;

    @Column
    private Date denyDate;

    @Column
    private Date departureDate;

    @Column
    private Date cancelDate;

    @Column
    private String observation;

    @ManyToOne
    private Residence residence;

    @ManyToOne
    private Visitor visitor;

}
