package ua.com.valexa.db.repository.red;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.red.GovUa07;
import ua.com.valexa.db.model.red.GovUa08;

import java.util.UUID;

@Repository
public interface Govua08Repository extends JpaRepository<GovUa08, UUID> {



}
