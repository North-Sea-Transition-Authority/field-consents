package uk.co.nstauthority.fieldconsents.application;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationVersionRepository extends CrudRepository<ApplicationVersion, Integer> {
}
