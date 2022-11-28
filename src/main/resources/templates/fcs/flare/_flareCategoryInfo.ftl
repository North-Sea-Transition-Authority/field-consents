<#include '../layout/layout.ftl'>

<#macro flareCategoryInfo>
  <@fdsDetails.summaryDetails summaryTitle="Category information">
    <h2 class="govuk-heading-s">
      Category A
    </h2>
    <p class="govuk-body">
      Streams for the safe operation of the asset based on its current design and operating at optimum efficiency (excluding Category C).
    </p>
    <h2 class="govuk-heading-s">
      Category B
    </h2>
    <p class="govuk-body">
      Flaring occurring during normal operations beyond levels optimum for the installation.
    </p>
    <h2 class="govuk-heading-s">
      Category C
    </h2>
    <p class="govuk-body">
      Emergency disposal and gas streams required specifically for the operation of safety critical equipment/elements.
    </p>
  </@fdsDetails.summaryDetails>
</#macro>