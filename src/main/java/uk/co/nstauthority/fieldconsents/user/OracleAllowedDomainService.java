package uk.co.nstauthority.fieldconsents.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.teams.Team;

@Service
@Profile("!use-service-access-request")
public class OracleAllowedDomainService implements AllowedDomainService {
  @Override
  public boolean isAllowedDomain(String domain, Team team) {
    return true;
  }
}
