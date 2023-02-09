<#include '../layout/layout.ftl'>

<#assign pageTitle = "Application type"/>

<@defaultPage htmlTitle=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl(continueStartApplicationUrl)>
    <@fdsRadio.radioGroup
      path="form.applicationType"
      labelText="Select an application type"
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--xl">
      <#assign isFirstItem = true/>
      <#list applicationTypes as type, typeDisplayText>
        <@fdsRadio.radioItem path="form.applicationType" itemMap={type: typeDisplayText} isFirstItem=isFirstItem/>
        <#assign isFirstItem = false/>
      </#list>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Continue"
      linkSecondaryActionUrl="${springUrl(cancelUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>