package ua.com.valexa.db.repository.red;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.GovUa11;
import ua.com.valexa.db.model.red.GovUa12;

import java.util.UUID;

@Repository
public interface Govua12Repository extends JpaRepository<GovUa12, UUID> {



}
