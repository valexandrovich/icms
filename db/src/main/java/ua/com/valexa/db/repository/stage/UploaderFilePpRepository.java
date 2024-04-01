package ua.com.valexa.db.repository.stage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.sys.UploaderRevisionPP;

import java.util.UUID;

@Repository
public interface UploaderFilePpRepository extends JpaRepository<UploaderRevisionPP, UUID> {
}
