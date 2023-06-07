package uk.co.nstauthority.fieldconsents.energyportal.user;

import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class EnergyPortalUserDtoTestUtil {

  private EnergyPortalUserDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private Long webUserAccountId = 1L;
    private Long personId = 1L;
    private String title = "title";
    private String forename = "forename";
    private String surname = "surname";
    private String primaryEmailAddress = "email address";
    private String telephoneNumber = "telephone number";
    private boolean isSharedAccount = false;
    private boolean canLogin = true;

    private Builder() {}

    public Builder withWebUserAccountId(Long webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    public Builder withPersonId(Long personId) {
      this.personId = personId;
      return this;
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    public Builder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder withEmailAddress(String emailAddress) {
      this.primaryEmailAddress = emailAddress;
      return this;
    }

    public Builder withPhoneNumber(String phoneNumber) {
      this.telephoneNumber = phoneNumber;
      return this;
    }

    public Builder hasSharedAccount(boolean isSharedAccount) {
      this.isSharedAccount = isSharedAccount;
      return this;
    }

    public Builder canLogin(boolean canLogin) {
      this.canLogin = canLogin;
      return this;
    }

    public EnergyPortalUserDto build() {
      return new EnergyPortalUserDto(
          webUserAccountId,
          personId,
          title,
          forename,
          surname,
          primaryEmailAddress,
          telephoneNumber,
          isSharedAccount,
          canLogin
      );
    }
  }
}
