package ua.com.valexa.db.repository.red;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.GovUa10;
import ua.com.valexa.db.model.red.GovUa11;

import java.util.UUID;

@Repository
public interface Govua11Repository extends JpaRepository<GovUa11, UUID> {



}
