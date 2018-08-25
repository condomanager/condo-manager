package br.com.condo.manager.api.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "building")
@Data
@NoArgsConstructor
public class Building implements Serializable {

    private static final long serialVersionUID = -4108788278133915312L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "doors")
    private Integer doors;

}
