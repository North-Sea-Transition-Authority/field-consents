package uk.co.nstauthority.fieldconsents.application;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationRepository extends CrudRepository<Application, Integer> {
}
