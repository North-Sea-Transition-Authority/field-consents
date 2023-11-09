<#include '../layout/layout.ftl'>

<#assign pageTitle = "Gas injection"/>

<@defaultPage
htmlTitle=pageTitle
pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
    path="form.willGasBeInjected"
    labelText="Will gas be injected for the purpose of creating or increasing pressure support?"
    fieldsetHeadingSize="h1"
    fieldsetHeadingClass="govuk-fieldset__legend--l"
    >
      <@fdsRadio.radioYes path="form.willGasBeInjected"/>
      <@fdsRadio.radioNo path="form.willGasBeInjected"/>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
