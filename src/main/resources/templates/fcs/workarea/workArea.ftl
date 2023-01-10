<#include '../layout/layout.ftl'>

<#assign pageTitle = "Work area" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>

    <@fdsForm.htmlForm actionUrl=springUrl("/work-area")>
        <@fdsSearchSelector.searchSelectorRest
        path="form.assetKey"
        restUrl=springUrl("/data-sources/assets")
        labelText="Select field/facility"
        hintText="Please select a field/facility to work with"/>
        <@fdsAction.button buttonText="Manage field/facility" />
    </@fdsForm.htmlForm>

</@defaultPage>