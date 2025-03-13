package uk.co.nstauthority.fieldconsents.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

class AuditRevisionUtilTest {

  @Test
  void getFallbackAuditUser_userNotSet() {
    assertThat(AuditRevisionUtil.getFallbackAuditUser()).isNull();
  }

  @Test
  void withFallbackAuditUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    AuditRevisionUtil.withFallbackAuditUser(
        user,
        () -> assertThat(AuditRevisionUtil.getFallbackAuditUser()).isEqualTo(user)
    );

    assertThat(AuditRevisionUtil.getFallbackAuditUser()).isNull();
  }

  @Test
  void withFallbackAuditUser_runnableThrowsException() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    try {
      AuditRevisionUtil.withFallbackAuditUser(
          user,
          () -> {
            throw new IllegalStateException();
          }
      );
    } catch (IllegalStateException exception) {
      // Ignore
    }

    assertThat(AuditRevisionUtil.getFallbackAuditUser()).isNull();
  }
}
