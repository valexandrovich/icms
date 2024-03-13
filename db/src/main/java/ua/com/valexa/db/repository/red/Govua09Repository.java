package ua.com.valexa.db.repository.red;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.GovUa08;
import ua.com.valexa.db.model.red.GovUa09;

import java.util.UUID;

@Repository
public interface Govua09Repository extends JpaRepository<GovUa09, UUID> {



}
