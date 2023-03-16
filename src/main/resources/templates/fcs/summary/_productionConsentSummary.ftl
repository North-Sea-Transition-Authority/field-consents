<#include '../layout/layout.ftl'>
<#import '_emptySummary.ftl' as emptySummary>

<#-- @ftlvariable name="productionView" type="uk.co.nstauthority.fieldconsents.production.ProductionView" -->

<#macro productionConsentSummary productionView summaryHeading showConsentDays>
  <@fdsSummaryList.summaryListCard
    headingText=summaryHeading
    headingSize="h3"
    summaryListId="production-summary-card-list">
    <table class="govuk-table fcs-view-table">
      <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header">${productionView.periodHeading()}</th>
          <#if showConsentDays>
            <th class="govuk-table__header">${productionView.consentDaysHeading()}</th>
          </#if>
          <th class="govuk-table__header">${productionView.oilMinHeading()}</th>
          <th class="govuk-table__header">${productionView.oilMaxHeading()}</th>
          <th class="govuk-table__header">${productionView.gasMinHeading()}</th>
          <th class="govuk-table__header">${productionView.gasMaxHeading()}</th>
        </tr>
      </thead>
      <tbody class="govuk-table__body">
        <#list productionView.productionRows() as productionRow>
          <tr class="govuk-table__row">
            <th scope="row" class="govuk-table__header">${productionRow.rowPrompt()}</th>
            <#if showConsentDays>
              <td class="govuk-table__cell">${productionRow.consentDays()!""}</td>
            </#if>
            <td class="govuk-table__cell">${productionRow.oilMinValue()}</td>
            <td class="govuk-table__cell">${productionRow.oilMaxValue()}</td>
            <td class="govuk-table__cell">${productionRow.gasMinValue()}</td>
            <td class="govuk-table__cell">${productionRow.gasMaxValue()}</td>
          </tr>
        </#list>
      </tbody>
    </table>
  </@fdsSummaryList.summaryListCard>
</#macro>