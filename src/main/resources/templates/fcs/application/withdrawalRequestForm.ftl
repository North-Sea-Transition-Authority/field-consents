<#include '../layout/layout.ftl'>

<#assign pageTitle = "Withdrawal request"/>

<@defaultPage
htmlTitle=pageTitle
backLinkUrl=springUrl(backLinkUrl)>

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
