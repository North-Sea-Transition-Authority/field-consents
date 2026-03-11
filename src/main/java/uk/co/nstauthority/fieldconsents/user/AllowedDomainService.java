package uk.co.nstauthority.fieldconsents.user;

import uk.co.nstauthority.fieldconsents.teams.Team;

public interface AllowedDomainService {

  boolean isAllowedDomain(String domain, Team team);
}
