<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->

<#include "../layout/layout.ftl">

<@defaultPage
  htmlTitle=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
  >
    <@fdsForm.htmlForm actionUrl=springUrl(submitFormUrl)>
        <@fdsSearchSelector.searchSelectorRest path="form.organisationGroupId"
          restUrl=springUrl(organisationGroupSearchRestUrl)
          labelText="Which organisation group would you like to create a team for?"
          pageHeading=true
          selectorMinInputLength=2
          preselectedItems={}
          />
        <@fdsAction.button buttonText="Creat team for organisation group"/>
    </@fdsForm.htmlForm>
    <@fdsDetails.summaryDetails summaryTitle="The organisation group I want to create a team for is not listed">
      <p class="govuk-body">
        If the organisation group you want to create a team for is not shown in the list then you must contact
        the person responsible for managing organisations on the UK Energy Portal.
      </p>
    </@fdsDetails.summaryDetails>

</@defaultPage>