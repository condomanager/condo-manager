package br.com.condo.manager.arch.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "endpoint_audit")
@Data
@NoArgsConstructor
public class EndpointAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date")
    private Date date;

    @Column(name="user_id")
    private Long userId;

    @Column(name="action")
    private String action;

    @Column(name="payload")
    private String payload;

    @Column(name="execution_time")
    private Long executionTime;

    public EndpointAudit(Date date, Long userId, String action, String payload, Long executionTime) {
        this.date = date;
        this.userId = userId;
        this.action = action;
        this.payload = payload;
        this.executionTime = executionTime;
    }

    @Override
    public String toString() {
        return "EndpointAudit: \"" + action + "\" by SecurityCredentials of ID " + userId + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + " in " + executionTime + "ms [" + payload + "]";
    }
}
