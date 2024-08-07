<#include '../../../layout/layout.ftl'>
<#import '_consentBreachSummary.ftl' as consentBreachSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Record breach"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=captionTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea
      path="form.consentBreachText.inputValue"
      labelText="How has the consent been breached?"/>
    <@fdsAction.submitButtons
      primaryButtonText="Record breach"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
