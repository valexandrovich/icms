package ua.com.valexa.db.model.red;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "govua_09", schema = "red")
@Data
public class GovUa09  {
    @Id
    @Column(name = "hash")
    private UUID hash;

    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "update_date")
    private LocalDateTime updateDate;
    @Column(name = "disable_date")
    private LocalDateTime disableDate;

    @Column(name = "create_revision_id")
    private Long createRevisionId;
    @Column(name = "update_revision_id")
    private Long updateRevisionId;
    @Column(name = "disable_revision_id")
    private Long disableRevisionId;

    @Column(name = "is_unprocessable")
    private Boolean isUnprocessable;

    @Column(name = "unprocessable_comment")
    private String unprocessableComment;

    @Column(name = "is_overprocess")
    private Boolean isOverprocess;


//    nn;status;series;number;date_edit

    @Column(name = "id")
    private String id;
    @Column(name = "ovd")
    private String ovd;
    @Column(name = "category")
    private String category;
    @Column(name = "first_name_ua")
    private String firstNameUa;
    @Column(name = "last_name_ua")
    private String lastNameUa;
    @Column(name = "patronymic_name_ua")
    private String patronymicNameUa;

    @Column(name = "first_name_ru")
    private String firstNameRu;
    @Column(name = "last_name_ru")
    private String lastNameRu;
    @Column(name = "patronymic_name_ru")
    private String patronymicNameRu;

    @Column(name = "first_name_en")
    private String firstNameEn;
    @Column(name = "last_name_en")
    private String lastNameEn;
    @Column(name = "patronymic_name_en")
    private String patronymicNameEn;


    @Column(name = "birthday")
    private LocalDate birthday;
    @Column(name = "sex")
    private String sex;
    @Column(name = "lost_date")
    private LocalDate lostDate;
    @Column(name = "lost_place")
    private String lostPlace;
    @Column(name = "article_crim")
    private String articleCrim;
    @Column(name = "restraint")
    private String restraint;
    @Column(name = "contact")
    private String contact;
    @Column(name = "photoid")
    private String photoid;



//    public boolean isPp(){
//        if (
//                firmEdrpou != null
//                        && !firmEdrpou.isBlank()
//                        && firmEdrpou.length() == 10
//                &&firmEdrpou.matches("\\d+")
//
//        ){
//            return true;
//        }
//        return false;
//    }
//
//    public boolean isLe(){
//        if (firmEdrpou != null
//                && !firmEdrpou.isBlank()
//                && firmEdrpou.length() <= 8
//                && firmEdrpou.length() >= 5
//                &&firmEdrpou.matches("\\d+")
//        ){
//            return true;
//        }
//        return false;
//
//    }

    public void generateHash() {
        this.hash = UUID.nameUUIDFromBytes((

        id+
         ovd+
         category+
         firstNameUa+
         lastNameUa+
         patronymicNameUa+

         firstNameRu+
         lastNameRu+
         patronymicNameRu+

         firstNameEn+
         lastNameEn+
         patronymicNameEn+


         birthday+
         sex+
         lostDate+
         lostPlace+
         articleCrim+
         restraint+
         contact+
         photoid


        ).getBytes());
    }
}
