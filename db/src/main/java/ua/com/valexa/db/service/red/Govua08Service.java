package ua.com.valexa.db.service.red;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.db.model.red.GovUa07;
import ua.com.valexa.db.model.red.GovUa08;
import ua.com.valexa.db.repository.red.Govua01Repository;
import ua.com.valexa.db.repository.red.Govua09Repository;

import java.util.List;

@Service
public class Govua08Service {

    @Autowired
    Govua09Repository govua08Repository;

    @Autowired
    EntityManager entityManager;

    public List<GovUa08> findByNameLike(String lastName, String firstName, String patronymicName) {
        String jpql = "SELECT r FROM GovUa08 r " +
                 " WHERE (UPPER(r.lastNameUa) = :lastName OR UPPER(r.lastNameEn) = :lastName OR UPPER(r.lastNameRu) = :lastName ) " +
                " AND (UPPER(r.firstNameUa) = :firstName OR UPPER(r.firstNameEn) = :firstName  OR UPPER(r.firstNameRu) = :firstName ) " +
                " AND (UPPER(r.patronymicNameUa) = :patronymicName OR UPPER(r.patronymicNameEn) = :patronymicName OR UPPER(r.patronymicNameRu) = :patronymicName ) "
                ;
        return entityManager.createQuery(jpql, GovUa08.class)
                .setParameter("lastName", lastName.toUpperCase())
                .setParameter("firstName", firstName.toUpperCase())
                .setParameter("patronymicName", patronymicName.toUpperCase())
                .getResultList();
    }




}
