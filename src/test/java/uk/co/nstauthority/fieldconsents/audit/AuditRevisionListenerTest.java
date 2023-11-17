package uk.co.nstauthority.fieldconsents.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import uk.co.nstauthority.fieldconsents.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceSaml2Authentication;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

class AuditRevisionListenerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private final AuditRevisionListener auditRevisionListener = new AuditRevisionListener();

  @AfterAll
  static void tearDown() {
    SecurityContextHolder.setContext(new SecurityContextImpl(null));
  }

  @Test
  void newRevision_userInContext() {
    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    var auditRevision = new AuditRevision();

    auditRevisionListener.newRevision(auditRevision);

    assertThat(auditRevision.getUserWuaId()).isEqualTo(USER.wuaId());
  }

  @Test
  void newRevision_noPrincipal() {
    SecurityContextHolder.setContext(new SecurityContextImpl(new ServiceSaml2Authentication(null, Set.of())));

    var auditRevision = new AuditRevision();

    auditRevisionListener.newRevision(auditRevision);

    assertThat(auditRevision.getUserWuaId()).isNull();
  }

  @Test
  void newRevision_noAuthenticationInContext() {
    SecurityContextHolder.setContext(new SecurityContextImpl(null));

    var auditRevision = new AuditRevision();

    auditRevisionListener.newRevision(auditRevision);

    assertThat(auditRevision.getUserWuaId()).isNull();
  }
}
