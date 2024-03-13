package ua.com.valexa.db.repository.red;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.GovUa01;
import ua.com.valexa.db.model.red.GovUa07;

import java.util.UUID;

@Repository
public interface Govua07Repository extends JpaRepository<GovUa07, UUID> {



}
