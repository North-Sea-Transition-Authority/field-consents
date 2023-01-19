<#include '../layout/layout.ftl'>

<#assign pageTitle = "Add another field" />

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=PageSize.TWO_THIRDS_COLUMN
>

  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorRest
    path="form.assetKey"
    restUrl=springUrl("/data-sources/fields")
    labelText=""/>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>

</@defaultPage>