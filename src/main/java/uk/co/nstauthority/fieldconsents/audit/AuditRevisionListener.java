package uk.co.nstauthority.fieldconsents.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;

@Service
public class AuditRevisionListener implements RevisionListener {

  private final UserDetailService userDetailService;

  @Autowired
  public AuditRevisionListener(UserDetailService userDetailService) {
    this.userDetailService = userDetailService;
  }

  @Override
  public void newRevision(Object revision) {
    var auditRevision = (AuditRevision) revision;
    var user = userDetailService.getUserDetail();
    auditRevision.setUserWuaId(user.wuaId());
  }
}
