package ua.com.valexa.db.model.sys;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(schema ="sys", name = "stored_step")
@Data
public class StoredStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_order")
    private Integer stepOrder;
    @Column(name = "service_name")
    private String serviceName;
    @Column(name = "worker_name")
    private String workerName;

    @ManyToOne
    @JoinColumn(name = "stored_job_id", foreignKey = @ForeignKey(name = "stored_step__stored_job__fk"))
    @JsonBackReference
    private StoredJob storedJob;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    @Column(name = "is_skippable")
    private Boolean isSkippable = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sys", name = "stored_step_parameter", joinColumns = @JoinColumn(name = "stored_step_id"), foreignKey = @ForeignKey(name = "stored_step_parameter__stored_step__fk"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public String toString() {
        return "StoredStep{" +
                "id=" + id +
                ", stepOrder=" + stepOrder +
                ", serviceName='" + serviceName + '\'' +
                ", workerName='" + workerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredStep that = (StoredStep) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
