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
@Table(name = "govua_13", schema = "red")
@Data
public class GovUa13 {
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
    @Column(name = "series")
    private String series;
    @Column(name = "number")
    private String number;
    @Column(name = "type")
    private String type;
    @Column(name = "status")
    private String status;
    @Column(name = "theft_date")
    private LocalDate theftDate;
    @Column(name = "insert_date")
    private LocalDate insertDate;


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
                id +
                        ovd +
                        series +
                        number +
                        type +
                        status +
                        theftDate +
                        insertDate
        ).getBytes());
    }
}
