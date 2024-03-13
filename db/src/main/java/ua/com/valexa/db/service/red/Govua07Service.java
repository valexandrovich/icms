package ua.com.valexa.db.service.red;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.db.model.red.GovUa01;
import ua.com.valexa.db.model.red.GovUa07;
import ua.com.valexa.db.repository.red.Govua01Repository;

import java.util.List;

@Service
public class Govua07Service {

    @Autowired
    Govua01Repository govua07Repository;

    @Autowired
    EntityManager entityManager;

    public List<GovUa07> findByCodeLike(String code) {
        String jpql = "SELECT r FROM GovUa07 r " +
                "WHERE UPPER(r.debtorCode) LIKE :code";
        return entityManager.createQuery(jpql, GovUa07.class)
                .setParameter("code", '%' + code + '%')
                .getResultList();
    }

    public List<GovUa07> findByNameLike(String name) {
        String jpql = "SELECT r FROM GovUa07 r " +
                "WHERE UPPER(r.debtorName) LIKE :name";
        return entityManager.createQuery(jpql, GovUa07.class)
                .setParameter("name", '%' + name + '%')
                .getResultList();
    }


}
