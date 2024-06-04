<#include '../layout/layout.ftl'>

<#assign pageTitle = "Add field" />

<@defaultPage
  htmlTitle=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorRest
      path="form.assetKey"
      restUrl=springUrl("/data-sources/field-assets")
      labelText="Add another field"
      pageHeading=true
      hintText="Add all fields that will be sources of products that you wish to be covered by this consent."
      labelHeadingClass="govuk-label--xl"/>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
