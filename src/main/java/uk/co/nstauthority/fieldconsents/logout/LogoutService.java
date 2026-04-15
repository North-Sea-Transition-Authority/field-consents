package uk.co.nstauthority.fieldconsents.logout;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.starter.accounts.EnergyPortalLogoutService;

@Service
class LogoutService implements EnergyPortalLogoutService {

  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

  LogoutService(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  /**
   * Deletes the spring sessions for all the principals that have a name that matches the provided wuaId.
   * @param wuaId the web user account id of the user to be logged out of the application
   */
  @Override
  public void logoutUser(Long wuaId) {
    var sessions = sessionRepository.findByPrincipalName(wuaId.toString());
    sessions.keySet().forEach(sessionRepository::deleteById);
  }
}
