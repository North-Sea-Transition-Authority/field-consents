<#include '../layout/layout.ftl'>

<#macro referenceNumberFilter form>
  <@fdsSearch.searchFilterItem itemName="Reference number" expanded=form.referenceSearchTerm?has_content>
    <@fdsSearch.searchTextInput
      path="form.referenceNumber"
      labelText=""
      suffixScreenReaderPrompt="Application reference number"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro statusFilter form statusCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Status" expanded=form.statuses?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.statuses"
      checkboxes=statusCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro applicationTypeFilter form applicationTypeCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Application type" expanded=form.applicationTypes?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.applicationTypes"
      checkboxes=applicationTypeCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro durationFilter form durationCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Duration" expanded=form.durationTypes?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.durationTypes"
      checkboxes=durationCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro operatorFilter form prefilledOperator operatorSearchRestUrl>
  <@fdsSearch.searchFilterItem itemName="Primary operator" expanded=prefilledOperator.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path="form.operatorId"
      restUrl=springUrl(operatorSearchRestUrl)
      labelText=""
      preselectedItems={prefilledOperator.id() : prefilledOperator.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>
