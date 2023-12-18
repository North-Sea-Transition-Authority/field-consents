<#include '../layout/layout.ftl'>
<#import './_ventCategoryInfo.ftl' as ventCategoryInfo>
<#import '../flarevent/_reportGasDataRow.ftl' as reportGasDataRow>
<#import '../hints/copyPasteTableHint.ftl' as copyPasteTableHint>

<#assign pageTitle = "Vent report gas properties"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList pageSize=PageSize.TWO_THIRDS_COLUMN>

    <@ventCategoryInfo.ventCategoryInfo/>
    <@copyPasteTableHint.hint/>

    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <table class="govuk-table" data-module="fcs-table-with-pastable-content">
        <caption class="govuk-table__caption govuk-table__caption--m">${reportPeriodStart} to ${reportPeriodEnd}</caption>
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header"></th>
          <th class="govuk-table__header">Category A</th>
          <th class="govuk-table__header">Category B</th>
          <th class="govuk-table__header">Category C</th>

            <@reportGasDataRow.reportGasDataRow
            rowDescription="Standard density (${standardDensityUnit})"
            categoryAData="form.categoryADensity"
            categoryBData="form.categoryBDensity"
            categoryCData="form.categoryCDensity"
            />

            <@reportGasDataRow.reportGasDataRow
            rowDescription="Inert gas content (${gasContentUnit})"
            categoryAData="form.categoryAInertGasPercentage"
            categoryBData="form.categoryBInertGasPercentage"
            categoryCData="form.categoryCInertGasPercentage"
            />

            <@reportGasDataRow.reportGasDataRow
            rowDescription="Hydrocarbon content (${gasContentUnit})"
            categoryAData="form.categoryAHydrocarbonPercentage"
            categoryBData="form.categoryBHydrocarbonPercentage"
            categoryCData="form.categoryCHydrocarbonPercentage"
            />
        </tbody>
      </table>
        <@fdsRadio.radioGroup
        path="form.evaluatedPerCategory"
        labelText="Have you evaluated the properties for each category individually?"
        hiddenContent=true
        >
            <@fdsRadio.radioYes path="form.evaluatedPerCategory"/>
            <@fdsRadio.radioNo path="form.evaluatedPerCategory">
                <@fdsTextarea.textarea
                path="form.evaluatedPerCategoryExplanation.inputValue"
                nestingPath="form.evaluatedPerCategory"
                labelText="Please provide an explanation why you haven’t evaluated the properties for each category"
                />
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons
        primaryButtonText="Save and continue"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>
</@defaultPage>
