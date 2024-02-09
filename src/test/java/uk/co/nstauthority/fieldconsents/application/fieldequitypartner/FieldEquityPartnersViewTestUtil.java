package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.List;

public class FieldEquityPartnersViewTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private List<String> fieldEquityPartnerNames = List.of("BP", "SHELL");

    public Builder withFieldEquityPartnerNames(List<String> fieldEquityPartnerNames) {
      this.fieldEquityPartnerNames = fieldEquityPartnerNames;
      return this;
    }

    public FieldEquityPartnersView build() {
      return new FieldEquityPartnersView(fieldEquityPartnerNames);
    }

    private Builder() {
    }
  }


}
