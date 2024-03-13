package ua.com.valexa.db.service.red;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.db.model.red.GovUa11;
import ua.com.valexa.db.model.red.GovUa12;

import java.util.List;

@Service
public class Govua12Service {


    @Autowired
    EntityManager entityManager;

    public List<GovUa12> findByPassportNumber(String serial, String number) {
        String jpql = "SELECT r FROM GovUa12 r " +
                 " WHERE (r.series = :serial OR r.series = '') and r.number = :number"
                ;
        return entityManager.createQuery(jpql, GovUa12.class)
                .setParameter("serial", serial)
                .setParameter("number", number)
                .getResultList();
    }




}
