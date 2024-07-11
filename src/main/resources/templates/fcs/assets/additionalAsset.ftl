<#include '../layout/layout.ftl'>
<#include '../flarevent/hintText.ftl'>

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
      hintText=ADDITIONAL_ASSET_HINT_TEXT
      labelHeadingClass="govuk-label--xl"
      selectorMinInputLength=2/>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
