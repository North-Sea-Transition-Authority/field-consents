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
  public void saveApplicationFlag(ApplicationVersion applicationVersion,
                                  ApplicationFlagType flagType,
                                  Boolean flagValue) {

    ApplicationFlag applicationFlag = new ApplicationFlag();
    applicationFlag.setFlagType(flagType);
    applicationFlag.setFlagValue(flagValue);
    applicationFlag.setApplicationVersion(applicationVersion);

    applicationFlagRepository.save(applicationFlag);
  }

  @Transactional
  public void deleteApplicationFlag(ApplicationVersion applicationVersion, ApplicationFlagType flagType) {
    applicationFlagRepository.deleteByApplicationVersionAndFlagType(applicationVersion, flagType);
  }
}
