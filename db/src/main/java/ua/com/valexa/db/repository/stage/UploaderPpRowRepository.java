package ua.com.valexa.db.repository.stage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.UploaderPpRow;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploaderPpRowRepository extends JpaRepository<UploaderPpRow, UUID> {
    List<UploaderPpRow> findByUploaderFileId(UUID uploaderFileId);
}
