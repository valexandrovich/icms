package ua.com.valexa.db.model.sys;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import ua.com.valexa.common.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(schema ="sys", name = "step",
        indexes = {
                @Index(name = "step__started_at_index", columnList = "started_at")
        }
)
@Data
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", foreignKey = @ForeignKey(name = "step__job__fk"))
    @JsonBackReference
    private Job job;

    @ManyToOne
    @JoinColumn(name = "stored_step_id", foreignKey = @ForeignKey(name = "step__stored_step__fk"))
    private StoredStep storedStep;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private Double progress;

    @Column(columnDefinition = "text")
    private String comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(id, step.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}