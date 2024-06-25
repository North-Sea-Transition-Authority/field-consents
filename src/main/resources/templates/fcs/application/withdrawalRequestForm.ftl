<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Withdrawal request"/>

<@defaultPage
  htmlTitle=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList
>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@fdsTextarea.textarea
      path="form.requestText.inputValue"
      pageHeading=true
      labelText="Enter the reason for withdrawing the application"
      caption=applicationReference/>
    <@fdsAction.submitButtons
      primaryButtonText="Submit request"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
