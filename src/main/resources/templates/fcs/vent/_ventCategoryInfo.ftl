<#include '../layout/layout.ftl'>

<#macro ventCategoryInfo>
  <@fdsDetails.summaryDetails summaryTitle="Category information">
    <h2 class="govuk-heading-s">
      Category A
    </h2>
    <p class="govuk-body">
      Streams for the safe operation of the asset based on its current design and operating at optimum efficiency (excluding Category C). The vent rate is for inerts + hydrocarbons (Energy Act 1976).
    </p>
    <h2 class="govuk-heading-s">
      Category B
    </h2>
    <p class="govuk-body">
      Venting occurring during normal operations beyond levels optimum for the installation. The vent rate is for inerts + hydrocarbons (Energy Act 1976).
    </p>
    <h2 class="govuk-heading-s">
      Category C
    </h2>
    <p class="govuk-body">
      Emergency disposal and gas streams required specifically for the operation of safety critical equipment/elements. The vent rate is for inerts + hydrocarbons (Energy Act 1976).
    </p>
  </@fdsDetails.summaryDetails>
</#macro>