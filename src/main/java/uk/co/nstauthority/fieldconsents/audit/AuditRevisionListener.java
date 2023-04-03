package uk.co.nstauthority.fieldconsents.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.stereotype.Service;

@Service
public class AuditRevisionListener implements RevisionListener {

  @Override
  public void newRevision(Object revision) {
    var auditRevision = (AuditRevision) revision;

    // TODO - FCS-5: Update this with the User's wua_id when available
    auditRevision.setUserWuaId(1);
  }
}
