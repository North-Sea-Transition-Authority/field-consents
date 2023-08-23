<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
caption=applicationReference
backLinkUrl=springUrl(backLinkUrl)
errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsDatePicker.datePicker
      path="form.deadlineDate"
      labelText="Deadline date"/>
    <@fdsTimeInput.timeInput
      labelText="Deadline time"
      formId="consultation-deadline-time"
      hoursPath="form.deadlineHours"
      minutesPath="form.deadlineMinutes"/>
    <@fdsAction.submitButtons
      primaryButtonText="Send request"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
