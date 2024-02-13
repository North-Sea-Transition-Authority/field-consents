<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="consentDataView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView" -->

<#macro summaryCard applicationType consentLengthType consentDataView consentFigureUnitView editUrl>
  <#assign summaryCardActions>
    <@fdsSummaryList.summaryListCardActionItem
      itemUrl=springUrl(editUrl)
      itemText="Edit"
      itemScreenReaderText="Edit consent data"
    />
  </#assign>
  <@fdsSummaryList.summaryListCard
    headingText="Consent data"
    headingSize="h3"
    summaryListId="summary-data-card-list"
    cardActionsContent=summaryCardActions
  >
    <@fdsSummaryList.summaryListRowNoAction keyText="Consent start date">
      ${consentDataView.consentStartDate()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Consent end date">
      ${consentDataView.consentEndDate()}
    </@fdsSummaryList.summaryListRowNoAction>

    <#if applicationType.name() == "PRODUCTION">
      <@fdsSummaryList.summaryListRowNoAction keyText="Consent figures">
        <table class="govuk-table">
          <thead class="govuk-table__head">
            <tr class="govuk-table__row">
              <#if consentLengthType.name() == "LONG_TERM">
                <th class="govuk-table__header">Year</th>
              </#if>
                <th class="govuk-table__header">Minimum oil (${consentFigureUnitView.productionOilUnit().getDisplayName()})</th>
                <th class="govuk-table__header">Maximum oil (${consentFigureUnitView.productionOilUnit().getDisplayName()})</th>
                <th class="govuk-table__header">Minimum gas (${consentFigureUnitView.productionGasUnit().getDisplayName()})</th>
                <th class="govuk-table__header">Maximum gas (${consentFigureUnitView.productionGasUnit().getDisplayName()})</th>
            </tr>
          </thead>
          <tbody class="govuk-table__body">
            <#if consentLengthType.name() == "SHORT_TERM" || consentLengthType.name() == "ANNUAL">
              <tr class="govuk-table__row">
                <@consentProductionFiguresCells consentDataView.shortTermOrAnnualConsentProductionFiguresView() />
              </tr>
            <#elseif consentLengthType.name() == "LONG_TERM">
              <#list consentDataView.longTermConsentProductionFiguresViews() as year, consentProductionFiguresView>
                <tr class="govuk-table__row">
                  <td class="govuk-table__cell">${year}</td>
                  <@consentProductionFiguresCells consentProductionFiguresView />
                </tr>
              </#list>
            </#if>
          </tbody>
        </table>
      </@fdsSummaryList.summaryListRowNoAction>
    <#elseif applicationType.name() == "FLARE" || applicationType.name() == "VENT">
      <@fdsSummaryList.summaryListRowNoAction keyText="Daily average (${consentFigureUnitView.emissionAverageUnit().getDisplayName()})">
        ${consentDataView.emissionDailyAverage()}
      </@fdsSummaryList.summaryListRowNoAction>
    </#if>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro consentProductionFiguresCells consentProductionFiguresView>
  <td class="govuk-table__cell">${consentProductionFiguresView.minOil()}</td>
  <td class="govuk-table__cell">${consentProductionFiguresView.maxOil()}</td>
  <td class="govuk-table__cell">${consentProductionFiguresView.minGas()}</td>
  <td class="govuk-table__cell">${consentProductionFiguresView.maxGas()}</td>
</#macro>
