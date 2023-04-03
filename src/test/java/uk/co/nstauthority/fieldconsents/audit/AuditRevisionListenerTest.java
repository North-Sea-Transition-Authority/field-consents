package uk.co.nstauthority.fieldconsents.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditRevisionListenerTest {

  @InjectMocks
  AuditRevisionListener auditRevisionListener;

  @Test
  void newRevision() {
    var auditRevision = new AuditRevision();

    auditRevisionListener.newRevision(auditRevision);

    // TODO - FCS-5: Update this with the User's wua_id when available
    assertThat(auditRevision.getUserWuaId()).isEqualTo(1);
  }
}