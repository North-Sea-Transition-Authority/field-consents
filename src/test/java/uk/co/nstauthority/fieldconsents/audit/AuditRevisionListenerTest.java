package uk.co.nstauthority.fieldconsents.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;

@ExtendWith(MockitoExtension.class)
class AuditRevisionListenerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private AuditRevisionListener auditRevisionListener;

  @BeforeEach
  void setUp() {
    when(userDetailService.getUserDetail()).thenReturn(USER);
  }

  @Test
  void newRevision() {
    var auditRevision = new AuditRevision();

    auditRevisionListener.newRevision(auditRevision);

    assertThat(auditRevision.getUserWuaId()).isEqualTo(USER.wuaId());
  }
}
