package uk.co.nstauthority.fieldconsents.application;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationVersionRepository extends CrudRepository<ApplicationVersion, Integer> {

  List<ApplicationVersion> findAllByApplicationIdOrderByVersion(Integer applicationId);
}