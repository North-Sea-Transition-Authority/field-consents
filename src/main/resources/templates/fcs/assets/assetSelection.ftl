<#include '../layout/layout.ftl'>

<@defaultPage htmlTitle=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl("/manage-asset")>
    <@fdsSearchSelector.searchSelectorRest
      path="form.assetKey"
      restUrl=springUrl("/data-sources/user-assets")
      labelText="Select field or facility"
      pageHeading=true
      labelHeadingClass="govuk-label--xl"
      hintText="Please select a field or a facility to work with."
      selectorMinInputLength=2/>
    <@fdsAction.button buttonText="Manage field or facility" />
  </@fdsForm.htmlForm>
</@defaultPage>
