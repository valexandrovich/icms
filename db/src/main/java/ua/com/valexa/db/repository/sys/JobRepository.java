package ua.com.valexa.db.repository.sys;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.valexa.db.model.sys.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
}
