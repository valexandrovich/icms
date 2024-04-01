package ua.com.valexa.db.repository.data.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.db.model.data.tag.TagType;

import java.util.UUID;

@Repository
public interface TagTypeRepository extends JpaRepository<TagType, UUID> {
}
