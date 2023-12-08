package uk.co.nstauthority.fieldconsents.application;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationVersionRepository extends CrudRepository<ApplicationVersion, Integer> {

  List<ApplicationVersion> findAllByApplicationIdOrderByVersion(Integer applicationId);

  @Query(
      """
      FROM ApplicationVersion av
      WHERE av.application.id IN :applicationIds
      AND av.status != 'DELETED'
      AND av.version = (
        SELECT MAX(av2.version)
        FROM ApplicationVersion av2
        WHERE av2.application = av.application
        AND av2.status != 'DELETED')
      """
  )
  List<ApplicationVersion> findLatestByApplicationIds(Collection<Integer> applicationIds);


  @Query(
      """
      SELECT DISTINCT av.caseOfficerWuaId
      FROM ApplicationVersion av
      WHERE av.status = :status
      AND av.version = (
        SELECT MAX (av2.version)
        FROM ApplicationVersion av2
        WHERE av2.application = av.application
        AND av2.status != 'DELETED')
      AND av.caseOfficerWuaId IS NOT NULL
      """
  )
  List<Long> findAllCaseOfficerWuaIdsByApplicationVersionStatus(ApplicationVersionStatus status);
}
