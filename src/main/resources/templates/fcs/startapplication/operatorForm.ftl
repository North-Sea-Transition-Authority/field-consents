<#include '../layout/layout.ftl'>

<#assign pageTitle = "Primary operator"/>

<@defaultPage htmlTitle=pageTitle>
  <@fdsForm.htmlForm actionUrl=springUrl(createApplicationUrl)>
    <@spring.bind "form.applicationType"/>
    <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
    <@fdsSearchSelector.searchSelectorRest
      path="form.organisationUnitId.inputValue"
      restUrl=springUrl(organisationUnitSearchRestUrl)
      labelText="Who is the primary operator?"
      pageHeading=true
      labelHeadingClass="govuk-label--xl"
      preselectedItems={prefilledOperator.id(): prefilledOperator.text()}
      hintText="The field or facility operator has been preselected here."
      selectorMinInputLength=2
    />
      <@fdsDetails.summaryDetails
        summaryTitle="The primary operator I want to select is not shown in the list"
      >
        <p class="govuk-body">
          If the primary operator you want to select is not shown in the list then you can <@requestNewCompany.requestCompanyLink/>
        </p>
      </@fdsDetails.summaryDetails>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Save and continue"
      linkSecondaryActionUrl="${springUrl(cancelUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>
