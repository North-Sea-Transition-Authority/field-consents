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
        restUrl=springUrl("/assets")
        labelText="Select field/terminal"
        hintText="Please select a field/terminal to work with"/>
        <@fdsAction.button buttonText="Manage field/terminal" />
    </@fdsForm.htmlForm>

</@defaultPage>