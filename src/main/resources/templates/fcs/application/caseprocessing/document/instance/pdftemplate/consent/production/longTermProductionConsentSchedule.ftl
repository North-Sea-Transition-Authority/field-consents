<div class="govuk-body govuk-!-text-align-centre fcs-schedule-header">
  <p><strong>SCHEDULE 1</strong></p>
  <p><strong>${primaryFieldName} – ${capitalizedConsentLengthType} Development and Production Consent</strong></p>
  <p><strong>Thousand Cubic Meters Per Day</strong></p>
</div>

<table class="govuk-table fcs-schedule-table">
  <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">YEAR</th>
      <th class="govuk-table__header" scope="col">OIL (Minimum)</th>
      <th class="govuk-table__header" scope="col">OIL (Maximum)</th>
      <th class="govuk-table__header" scope="col">GAS (Minimum)</th>
      <th class="govuk-table__header" scope="col">GAS (Maximum)</th>
    </tr>
  </thead>
  <tbody class="govuk-table__body">
    <#list consentProductionFiguresViews as year, consentProductionFiguresView>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${year}<#if year?is_first> (1)</#if><#if year?is_last> (2)</#if></td>
        <td class="govuk-table__cell">${consentProductionFiguresView.minOil()}</td>
        <td class="govuk-table__cell">${consentProductionFiguresView.maxOil()}</td>
        <td class="govuk-table__cell">${consentProductionFiguresView.minGas()}</td>
        <td class="govuk-table__cell">${consentProductionFiguresView.maxGas()}</td>
      </tr>
    </#list>
  </tbody>
</table>

<p class="govuk-body">NOTES:</p>

<p class="govuk-body govuk-!-margin-bottom-0">(1) Production from ${productionFromDate}.</p>
<p class="govuk-body">(2) Production to ${consentEndDate}.</p>
