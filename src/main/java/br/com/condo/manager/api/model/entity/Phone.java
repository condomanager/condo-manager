package br.com.condo.manager.api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "phone")
@Data
@NoArgsConstructor
public class Phone implements Serializable {

    private static final long serialVersionUID = -8093650591362727469L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String identifier;

    @Column
    private String number;

    @JsonIgnore
    @ManyToOne
    private Profile profile;

}
