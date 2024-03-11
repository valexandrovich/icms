package ua.com.valexa.db.model.sys;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(schema = "sys", name = "job",
        indexes = {
                @Index(name = "job__started_at_index", columnList = "started_at")
        }
)
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "initiator_name")
    private String initiatorName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sys", name = "job_result", joinColumns = @JoinColumn(name = "job_id"), foreignKey = @ForeignKey(name = "job_result__job__fk"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> results = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "stored_job_id", foreignKey = @ForeignKey(name = "job__stored_job__fk"))
    private StoredJob storedJob;

    @OneToMany(mappedBy = "job")
    @JsonManagedReference
    private Set<Step> steps = new HashSet<>();


}
