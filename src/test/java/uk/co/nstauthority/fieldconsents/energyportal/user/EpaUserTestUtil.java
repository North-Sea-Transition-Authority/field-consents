package uk.co.nstauthority.fieldconsents.energyportal.user;

import java.util.UUID;
import uk.co.fivium.energyportalapi.generated.types.AccountStatus;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

class EpaUserTestUtil {

  private EpaUserTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder Builder() {
    return new Builder();
  }

  static class Builder {

    private int webUserAccountId = 1;
    private int personId = 2;
    private String loginId = UUID.randomUUID().toString();
    private String title = "title";
    private String forename = "forename";
    private String surname = "surname";
    private String middleInitials = "initial";
    private String primaryEmailAddress = "email address";
    private String telephoneNumber = "telephone number";
    private boolean canLogin = true;
    private boolean isAccountShared = false;
    private AccountStatus accountStatus = AccountStatus.NEW;
    private Builder() {}

    Builder withWebUserAccountId(int webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    Builder withPersonId(int personId) {
      this.personId = personId;
      return this;
    }

    Builder withLoginId(String loginId) {
      this.loginId = loginId;
      return this;
    }

    Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    Builder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    Builder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    Builder withInitials(String initials) {
      this.middleInitials = initials;
      return this;
    }

    Builder withEmailAddress(String emailAddress) {
      this.primaryEmailAddress = emailAddress;
      return this;
    }

    Builder withPhoneNumber(String phoneNumber) {
      this.telephoneNumber = phoneNumber;
      return this;
    }

    Builder canLogin(boolean canLogin) {
      this.canLogin = canLogin;
      return this;
    }

    Builder isSharedAccount(boolean isAccountShared) {
      this.isAccountShared = isAccountShared;
      return this;
    }

    Builder withAccountStatus(AccountStatus accountStatus) {
      this.accountStatus = accountStatus;
      return this;
    }

    User build() {
      return new User(
          webUserAccountId,
          personId,
          loginId,
          title,
          forename,
          surname,
          middleInitials,
          primaryEmailAddress,
          telephoneNumber,
          canLogin,
          isAccountShared,
          accountStatus
      );
    }

  }

}
