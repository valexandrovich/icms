package ua.com.valexa.db.model.data.tag;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(schema = "data", name = "tag_type", uniqueConstraints = {
        @UniqueConstraint(name = "tag_type__code__uindex", columnNames = "code")
})
@Data
public class TagType {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String description;

    @Column(name="code_level_1", nullable = false)
    private String code_level_1;
    @Column(name="description_level_1",  nullable = false)
    private String description_level_1;

    @Column(name="code_level_2")
    private String code_level_2;
    @Column(name="description_level_2")
    private String description_level_2;

    @Column(name="code_level_3")
    private String code_level_3;
    @Column(name="description_level_3")
    private String description_level_3;

    @Column(name="code_level_4")
    private String code_level_4;
    @Column(name="description_level_4")
    private String description_level_4;

    @Column(name="code_level_5")
    private String code_level_5;
    @Column(name="description_level_5")
    private String description_level_5;


    private void setFulls(){
        StringBuilder fullCode = new StringBuilder();
        StringBuilder fullDescription = new StringBuilder();

        if (code_level_1 != null && !code_level_1.isEmpty()){
            fullCode.append(code_level_1);
        }

        if (description_level_1 != null && !description_level_1.isEmpty()){
            fullDescription.append(description_level_1);
        }


        if (code_level_2 != null && !code_level_2.isEmpty()){
            fullCode.append(code_level_2);
        }

        if (description_level_2 != null && !description_level_2.isEmpty()){
            fullDescription.append(" / " + description_level_2);
        }



        if (code_level_3 != null && !code_level_3.isEmpty()){
            fullCode.append(code_level_3);
        }

        if (description_level_3 != null && !description_level_3.isEmpty()){
            fullDescription.append(" / " + description_level_3);
        }




        if (code_level_4 != null && !code_level_4.isEmpty()){
            fullCode.append(code_level_4);
        }

        if (description_level_4 != null && !description_level_4.isEmpty()){
            fullDescription.append(" / " + description_level_4);
        }



        if (code_level_5 != null && !code_level_5.isEmpty()){
            fullCode.append(code_level_5);
        }

        if (description_level_5 != null && !description_level_5.isEmpty()){
            fullDescription.append(" / " + description_level_5);
        }

        this.code = fullCode.toString();
        this.description = fullDescription.toString();

    }

    @Override
    public String toString() {
        setFulls();
        return this.code + "_" + this.description;
    }

    public void generateId(){
        setFulls();
        this.id = UUID.nameUUIDFromBytes(toString().getBytes());
    }

    //    private Boolean isLeAcceptable;
//    private Boolean isPiAcceptable;
}
