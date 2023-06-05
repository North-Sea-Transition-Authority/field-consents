package uk.co.nstauthority.fieldconsents.application.flags;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
public class ApplicationFlagService {

  private final ApplicationFlagRepository applicationFlagRepository;

  @Autowired
  public ApplicationFlagService(ApplicationFlagRepository applicationFlagRepository) {
    this.applicationFlagRepository = applicationFlagRepository;
  }

  public Optional<Boolean> findFlagValue(ApplicationVersion applicationVersion, ApplicationFlagType flagType) {
    Optional<ApplicationFlag> flagOptional = applicationFlagRepository
        .findByApplicationVersionAndFlagType(applicationVersion, flagType);

    return flagOptional.map(ApplicationFlag::getFlagValue);
  }

  @Transactional
  public void addApplicationFlag(ApplicationVersion applicationVersion,
                                 ApplicationFlagType flagType,
                                 Boolean flagValue) {

    var applicationFlag = new ApplicationFlag(applicationVersion, flagType, flagValue);
    applicationFlagRepository.save(applicationFlag);
  }

  @Transactional
  public void addOrUpdateApplicationFlag(ApplicationVersion applicationVersion,
                                         ApplicationFlagType flagType,
                                         Boolean flagValue) {
    var applicationFlag = applicationFlagRepository.findByApplicationVersionAndFlagType(applicationVersion, flagType)
        .orElseGet(() -> new ApplicationFlag(applicationVersion, flagType));

    applicationFlag.setFlagValue(flagValue);
    applicationFlagRepository.save(applicationFlag);
  }
}
