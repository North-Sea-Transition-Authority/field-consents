<#include '../layout/layout.ftl'>

<#assign pageTitle = "Add field" />

<@defaultPage
  htmlTitle=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorRest
      path="form.assetKey"
      restUrl=springUrl("/data-sources/fields")
      labelText="Add another field"
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>