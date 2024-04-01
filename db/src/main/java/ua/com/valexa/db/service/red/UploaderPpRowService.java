package ua.com.valexa.db.service.red;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.db.model.red.GovUa13;
import ua.com.valexa.db.model.red.UploaderPpRow;

import java.util.List;

@Service
public class UploaderPpRowService {


    @Autowired
    EntityManager entityManager;

    public List<UploaderPpRow> findByLocalPassportNumber(String serial, String number) {
        String jpql = "SELECT r FROM UploaderPpRow r " +
                 " WHERE (r.localPassportSerial = :serial AND r.localPassportNumber = :number) "
                ;
        return entityManager.createQuery(jpql, UploaderPpRow.class)
                .setParameter("serial", serial)
                .setParameter("number", number)
                .getResultList();
    }




}
