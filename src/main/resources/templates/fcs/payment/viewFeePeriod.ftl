<#include '../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsSummaryList.summaryListWrapper
    summaryListId="feeLines"
    headingText="Fee lines"
    headingClass="govuk-heading-m"
  >
    <table class="govuk-table govuk-!-width-full">
      <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Asset type</th>
          <th class="govuk-table__header" scope="col">Consent type</th>
          <th class="govuk-table__header" scope="col">Duration</th>
          <th class="govuk-table__header" scope="col">New/Revised consent</th>
          <th class="govuk-table__header" scope="col">Cost</th>
        </tr>
      </thead>

      <tbody class="govuk-table__body">
        <#list feeLineViews as feeLineView>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${feeLineView.mnemonic().assetType().getDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().applicationType().getDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().consentLengthType().getShortDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().consentRevisionType().getDisplayName()}</td>
            <td class="govuk-table__cell">£${feeLineView.formattedAmount()}</td>
          </tr>
        </#list>
      </tbody>
    </table>
  </@fdsSummaryList.summaryListWrapper>
</@defaultPage>
