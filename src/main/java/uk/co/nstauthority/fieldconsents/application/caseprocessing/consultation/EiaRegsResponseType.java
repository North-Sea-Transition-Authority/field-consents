package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

public enum EiaRegsResponseType implements ConsultationResponseType {

  AGREE(
      "Agree",
      "Provide consent conditions if they apply",
      """
          Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage
          (Environmental Impact Assessment) Regulations 2020, the Offshore Petroleum Regulator for
          Environment and Decommissioning, acting on behalf of the Secretary of State for Energy Security and Net Zero,
          agrees to the NSTA’s grant of consent for the activities described in application reference %s
          and a copy of the Secretary of State’s decision is attached.
          """,
      "form.eiaRegsAgreeDescription.inputValue",
      false,
      true
  ),
  DO_NOT_AGREE(
      "Do not agree",
      "Why do you not agree to this application?",
      """
          Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage
          (Environmental Impact Assessment) Regulations 2020, the Offshore Petroleum Regulator for
          Environment and Decommissioning, acting on behalf of the Secretary of State for Energy Security and Net Zero,
          does not agree to the NSTA’s grant of consent for the activities described in application reference %s
          and a copy of the Secretary of State’s decision is attached.
          """,
      "form.eiaRegsDoNotAgreeDescription.inputValue",
      true,
      true
  ),
  DOES_NOT_APPLY(
      "Agreement to consent not required under the EIA regulations",
      "Why is agreement not required under the EIA regulations?",
      """
          Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage
          (Environmental Impact Assessment) Regulations 2020, the Offshore Petroleum Regulator for
          Environment and Decommissioning, acting on behalf of the Secretary of State for Energy Security and Net Zero,
          does not consider that the Secretary of State’s agreement to the grant of consent by
          the NSTA for the activities described in application reference %s is required.
          """,
      "form.eiaRegsDoesNotApplyDescription.inputValue",
      false,
      false
  );

  private final String displayName;
  private final String textAreaDisplayText;
  private final String textAreaHintText;
  private final String textAreaInputName;
  private final boolean textAreaInputRequired;
  private final boolean secretaryOfStateDecisionRequired;

  EiaRegsResponseType(
      String displayName,
      String textAreaDisplayText,
      String textAreaHintText,
      String textAreaInputName,
      boolean textAreaInputRequired,
      boolean secretaryOfStateDecisionRequired
  ) {
    this.displayName = displayName;
    this.textAreaDisplayText = textAreaDisplayText;
    this.textAreaHintText = textAreaHintText;
    this.textAreaInputName = textAreaInputName;
    this.textAreaInputRequired = textAreaInputRequired;
    this.secretaryOfStateDecisionRequired = secretaryOfStateDecisionRequired;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getTextAreaDisplayText() {
    return textAreaDisplayText;
  }

  @Override
  public String getTextAreaHintText(String applicationReference) {
    return textAreaHintText.formatted(applicationReference);
  }

  @Override
  public String getTextAreaInputName() {
    return textAreaInputName;
  }


  @Override
  public boolean isTextAreaInputRequired() {
    return textAreaInputRequired;
  }

  @Override
  public boolean isSecretaryOfStateDecisionRequired() {
    return secretaryOfStateDecisionRequired;
  }
}
