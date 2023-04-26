<#include '../layout/layout.ftl'>

<@defaultPage htmlTitle=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl("/manage-asset")>
    <@fdsSearchSelector.searchSelectorRest
      path="form.assetKey"
      restUrl=springUrl("/data-sources/assets")
      labelText="Select field/facility"
      pageHeading=true
      labelHeadingClass="govuk-label--xl"
      hintText="Please select a field/facility to work with"/>
    <@fdsAction.button buttonText="Manage field/facility" />
  </@fdsForm.htmlForm>
</@defaultPage>