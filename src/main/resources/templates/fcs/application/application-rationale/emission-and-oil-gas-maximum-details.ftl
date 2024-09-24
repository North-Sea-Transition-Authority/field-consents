<#include '../../../fds/components/details/details.ftl'>

<#-- @ftlvariable name="emissionDailyAverage" type="uk.co.nstauthority.fieldconsents.application.rationale.emissions.EmissionDailyAverage" -->
<#-- @ftlvariable name="oilAndGasMaximums" type="uk.co.nstauthority.fieldconsents.application.rationale.production.OilAndGasMaximums" -->

<#macro emissionDailyAverageSection emissionDailyAverage>
  <@details
    detailsTitle="${emissionDailyAverage.year()} ${emissionDailyAverage.applicationType().getDisplayName()?lower_case} consent"
    detailsText="${emissionDailyAverage.emissionDailyAverage()} ${emissionDailyAverage.emissionAverageUnit().getDisplayName()}"
  />
</#macro>

<#macro oilAndGasMaximumsSection oilAndGasMaximums>
  <#assign detailsText>
    <p class="govuk-body">Oil: ${oilAndGasMaximums.oilMaximum()} ${oilAndGasMaximums.oilUnit().getDisplayName()}</p>
    <p class="govuk-body">Gas: ${oilAndGasMaximums.gasMaximum()} ${oilAndGasMaximums.gasUnit().getDisplayName()}</p>
  </#assign>
  <@details
    detailsTitle="${oilAndGasMaximums.year()} production consent oil and gas maximums"
    detailsText=detailsText
  />
</#macro>
