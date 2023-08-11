<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Assign technical reviewer"/>

<@defaultPage
  htmlTitle=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.technicalReviewerWuaId"
      options=technicalReviewerAssignmentCandidates
      labelText="Select a technical reviewer"
      caption=applicationReference
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Assign technical reviewer"
      linkSecondaryActionUrl="${springUrl(backLinkUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>
