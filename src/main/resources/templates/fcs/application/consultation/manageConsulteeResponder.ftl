<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage
  htmlTitle=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.allocatedResponder"
      options=availableRespondersMap
      labelText=pageTitle
      caption=applicationReference
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Assign responder"
      linkSecondaryActionUrl="${springUrl(backLinkUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>
