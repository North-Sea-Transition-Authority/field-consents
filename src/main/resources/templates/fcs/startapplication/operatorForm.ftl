<#include '../layout/layout.ftl'>

<#assign pageTitle = "Primary operator"/>

<@defaultPage htmlTitle=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl(createApplicationUrl)>
    <@spring.bind "form.applicationType"/>
    <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
    <@fdsSearchSelector.searchSelectorRest
      path="form.organisationUnitId.inputValue"
      restUrl=springUrl("/data-sources/organisation-units")
      labelText="Who is the primary operator?"
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Save and continue"
      linkSecondaryActionUrl="${springUrl(cancelUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>