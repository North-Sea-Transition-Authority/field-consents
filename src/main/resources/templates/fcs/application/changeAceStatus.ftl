<#include '../layout/layout.ftl'>

<#assign pageTitle = "Change ACE status"/>

<@defaultPage
htmlTitle=pageTitle
pageSize=PageSize.TWO_THIRDS_COLUMN
backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
      path="form.aceFlag"
      labelText="Is this an ACE application?"
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--xl">
      <@fdsRadio.radioYes path="form.aceFlag"/>
      <@fdsRadio.radioNo path="form.aceFlag"/>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
      primaryButtonText="Save"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
