package uk.co.nstauthority.fieldconsents.application.otherlegacydata;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface OtherLegacyDataRepository extends CrudRepository<OtherLegacyData, Integer> {
  Optional<OtherLegacyData> findByApplicationVersion(ApplicationVersion applicationVersion);
}
