<#include '../../layout/layout.ftl'>
<#import '_selectedApplications.ftl' as selectedApplicationsFtl>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_PAGE_WIDTH
  errorItems=errorList
>
  <@fdsForm.htmlForm>
    <#assign path="form.selectedAction"/>
    <@fdsRadio.radioGroup path=path labelText="Which action would you like to perform?">
      <#list availableActions as action>
        <@fdsRadio.radioItem
          path=path
          itemHintText="${action.getDescription()}"
          itemMap={action.name(): action.getDisplayName()}
        />
      </#list>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>