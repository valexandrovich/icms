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
@Table(name = "govua_01", schema = "red")
@Data
public class GovUa01 {
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

    @Column(name = "number")
    private Long number;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "type")
    private String type;
    @Column(name = "firm_edrpou")
    private String firmEdrpou;
    @Column(name = "firm_name")
    private String firmName;
    @Column(name = "case_number")
    private String caseNumber;
    @Column(name = "start_date_auc")
    private LocalDate startDateAuc;
    @Column(name = "end_date_auc")
    private LocalDate endDateAuc;
    @Column(name = "court_name")
    private String courtName;
    @Column(name = "end_registration_date")
    private LocalDate endRegistrationDate;

    public boolean isPp(){
        if (
                firmEdrpou != null
                        && !firmEdrpou.isBlank()
                        && firmEdrpou.length() == 10
                &&firmEdrpou.matches("\\d+")

        ){
            return true;
        }
        return false;
    }

    public boolean isLe(){
        if (firmEdrpou != null
                && !firmEdrpou.isBlank()
                && firmEdrpou.length() <= 8
                && firmEdrpou.length() >= 5
                &&firmEdrpou.matches("\\d+")
        ){
            return true;
        }
        return false;

    }

    public void generateHash() {
        this.hash = UUID.nameUUIDFromBytes((number.toString()
                + date
                + type
                + firmEdrpou
                + firmName
                + caseNumber
                + startDateAuc
                + endDateAuc
                + courtName
                + endRegistrationDate
        ).getBytes());
    }
}
