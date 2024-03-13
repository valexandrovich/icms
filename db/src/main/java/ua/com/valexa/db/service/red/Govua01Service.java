package ua.com.valexa.db.service.red;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.db.model.red.GovUa01;
import ua.com.valexa.db.repository.red.Govua01Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class Govua01Service {

    @Autowired
    Govua01Repository govua01Repository;

    @Autowired
    EntityManager entityManager;

    public List<GovUa01> findByCodeLike(String code) {
        String jpql = "SELECT r FROM GovUa01 r " +
                "WHERE UPPER(r.firmEdrpou) LIKE :firmEdrpou";
        return entityManager.createQuery(jpql, GovUa01.class)
                .setParameter("firmEdrpou", '%' + code + '%')
                .getResultList();
    }

    public List<GovUa01> findByNameLike(String name) {
        String jpql = "SELECT r FROM GovUa01 r " +
                "WHERE UPPER(r.firmName) LIKE :firmName";
        return entityManager.createQuery(jpql, GovUa01.class)
                .setParameter("firmName", '%' + name + '%')
                .getResultList();
    }


}
