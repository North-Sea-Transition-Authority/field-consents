<#include '../layout/layout.ftl'>

<#assign pageTitle = "Who is the primary operator?" />

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl(createApplicationUrl)>
    <@spring.bind "form.applicationType"/>
    <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
    <@fdsSearchSelector.searchSelectorRest
      path="form.organisationUnitId.inputValue"
      restUrl=springUrl("/data-sources/organisation-units")
      labelText=""/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Save and continue"
      linkSecondaryActionUrl="${springUrl(cancelUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>