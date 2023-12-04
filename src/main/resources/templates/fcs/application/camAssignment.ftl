<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Assign consents and authorisations manager"/>

<@defaultPage
htmlTitle=pageTitle
backLinkUrl=springUrl(backLinkUrl)
errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.camWuaId"
      options=camUserAssignmentCandidates
      labelText="Select a consents and authorisations manager"
      caption=applicationReference
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Assign CAM"
      linkSecondaryActionUrl="${springUrl(backLinkUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>
