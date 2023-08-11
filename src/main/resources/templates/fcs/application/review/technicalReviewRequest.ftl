<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Request a technical review"/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
caption=applicationReference
backLinkUrl=springUrl(backLinkUrl)
errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.technicalReviewerWuaId"
      options=technicalReviewerAssignmentCandidates
      labelText="Select a technical reviewer"/>
    <@fdsDatePicker.datePicker
      path="form.deadlineDate"
      labelText="Deadline date"/>
    <@fdsTimeInput.timeInput
      labelText="Deadline time"
      formId="technical-review-deadline-time"
      hoursPath="form.deadlineHours"
      minutesPath="form.deadlineMinutes"/>
    <@fdsTextarea.textarea
      path="form.requestText.inputValue"
      labelText="Notes for the reviewer"
      optionalLabel=true/>
    <@fdsAction.submitButtons
      primaryButtonText="Send request"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
