<#include '../layout/layout.ftl'>

<#macro referenceNumberFilter form path="form.referenceNumber">
  <@fdsSearch.searchFilterItem itemName="Reference number" expanded=form.referenceSearchTerm?has_content>
    <@fdsSearch.searchTextInput path=path labelText="" suffixScreenReaderPrompt="Application reference number"/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro statusFilter form statusCheckboxes path="form.statuses">
  <@fdsSearch.searchFilterItem itemName="Status" expanded=form.statuses?has_content>
    <@fdsSearch.searchCheckboxes path=path checkboxes=statusCheckboxes/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro applicationTypeFilter form applicationTypeCheckboxes path="form.applicationTypes">
  <@fdsSearch.searchFilterItem itemName="Application type" expanded=form.applicationTypes?has_content>
    <@fdsSearch.searchCheckboxes path=path checkboxes=applicationTypeCheckboxes/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro durationFilter form durationCheckboxes path="form.durationTypes">
  <@fdsSearch.searchFilterItem itemName="Duration" expanded=form.durationTypes?has_content>
    <@fdsSearch.searchCheckboxes path=path checkboxes=durationCheckboxes/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro operatorFilter form prefilledOperator operatorSearchRestUrl path="form.operatorId">
  <@fdsSearch.searchFilterItem itemName="Primary operator" expanded=prefilledOperator.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path=path
      restUrl=springUrl(operatorSearchRestUrl)
      labelText=""
      preselectedItems={prefilledOperator.id() : prefilledOperator.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro assetTypeWithShoreFilter form expanded assetTypeWithShoreCheckboxes path="form.assetTypesWithShore">
  <@fdsSearch.searchFilterItem itemName="Asset type" expanded=expanded>
    <@fdsSearch.searchCheckboxes path=path checkboxes=assetTypeWithShoreCheckboxes/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro submittedYearFilter form path="form.submittedYear">
  <@fdsSearch.searchFilterItem itemName="Submission year" expanded=form.submittedYear?has_content>
    <@fdsSearch.searchTextInput path=path labelText="" suffixScreenReaderPrompt="Application submission year"/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro geographicAreaFilter form expanded geographicAreaCheckboxes path="form.geographicAreas">
  <@fdsSearch.searchFilterItem itemName="Geographic area" expanded=expanded>
    <@fdsSearch.searchCheckboxes path=path checkboxes=geographicAreaCheckboxes />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro aceFilter form expanded aceCheckboxes path="form.aceFlagStatuses">
  <@fdsSearch.searchFilterItem itemName="ACE status" expanded=expanded>
    <@fdsSearch.searchCheckboxes path=path checkboxes=aceCheckboxes/>
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro fieldAssetFilter form prefilledField fieldAssetSearchRestUrl path="form.fieldAssetKey">
  <@fdsSearch.searchFilterItem itemName="Field" expanded=prefilledField.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path=path
      restUrl=springUrl(fieldAssetSearchRestUrl)
      labelText=""
      preselectedItems={prefilledField.id() : prefilledField.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro terminalAssetFilter form prefilledTerminal terminalAssetSearchRestUrl path="form.terminalAssetKey">
  <@fdsSearch.searchFilterItem itemName="Facility" expanded=prefilledTerminal.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path=path
      restUrl=springUrl(terminalAssetSearchRestUrl)
      labelText=""
      preselectedItems={prefilledTerminal.id() : prefilledTerminal.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro licenceReferenceFilter form path="form.licenceReference">
  <@fdsSearch.searchFilterItem itemName="Licence reference" expanded=form.licenceReference?has_content>
    <@fdsSearch.searchTextInput
      path=path
      labelText=""
      suffixScreenReaderPrompt="Licence reference"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro caseOfficerFilter form expanded caseOfficerOptions path="form.caseOfficerWuaId">
  <@fdsSearch.searchFilterItem itemName="Case officer" expanded=expanded>
    <@fdsSearchSelector.searchSelectorEnhanced
      path=path
      options=caseOfficerOptions
      labelText=""
      labelHeadingClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro approvedForIssueFilter form expanded path="form.approvedForIssue">
  <@fdsSearch.searchFilterItem itemName="Issuing status" expanded=expanded>
    <@fdsCheckbox.checkbox path=path labelText="Ready to grant and issue" smallCheckboxes=true/>
  </@fdsSearch.searchFilterItem>
</#macro>
