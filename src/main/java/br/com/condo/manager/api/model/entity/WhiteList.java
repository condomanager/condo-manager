package br.com.condo.manager.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "whiteList")
@Data
@NoArgsConstructor
public class WhiteList implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Date authorizeDate;

    @Column
    private Date cancelDate;

    @Column
    private String observation;

    @ManyToOne
    private Residence residence;

}
