package ua.com.valexa.db.model.sys;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema ="sys", name = "stored_job")
@Data
public class StoredJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "source_nick_name")
    private String sourceNickName;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @Column(name = "is_persistable")
    private boolean isPersistable = true;

    @OneToMany( fetch = FetchType.EAGER)
    @JoinColumn(name = "stored_job_id")
    @OrderBy("stepOrder")
    @JsonManagedReference
    private Set<StoredStep> steps = new HashSet<>();



}

