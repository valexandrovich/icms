package ua.com.valexa.db.model.sys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "sys", name = "uploader_revision_pp")
@Data
public class UploaderRevisionPP {
    @Id
    private UUID id;
    private LocalDateTime createDate;
    private String initiatorName;
    private String revisionName;
    private Integer rowsCount;

}
