<#include '../layout/layout.ftl'>

<#assign pageTitle = "Select an application type"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl(continueStartApplicationUrl)>
    <@fdsRadio.radioGroup path="form.applicationType" labelText="">
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