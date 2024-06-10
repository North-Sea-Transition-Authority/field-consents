<#include '../layout/layout.ftl'>

<#macro reportGasDataRow
  rowDescription=rowDescription
  categoryAData=categoryAData
  categoryBData=categoryBData
  categoryCData=categoryCData
>
  <tr class="govuk-table__row">
    <td class="govuk-table__cell">${rowDescription}</td>
    <td class="govuk-table__cell">
        <@fdsTextInput.textInput
        path="${categoryAData}.inputValue"
        labelText="${categoryAData}.displayName"
        formGroupClass="govuk-!-margin-bottom-0"
        labelClass="govuk-visually-hidden"/>
    </td>
    <td class="govuk-table__cell">
        <@fdsTextInput.textInput
        path="${categoryBData}.inputValue"
        labelText="${categoryBData}.displayName"
        formGroupClass="govuk-!-margin-bottom-0"
        labelClass="govuk-visually-hidden"/>
    </td>
    <td class="govuk-table__cell">
        <@fdsTextInput.textInput
        path="${categoryCData}.inputValue"
        labelText="${categoryCData}.displayName"
        formGroupClass="govuk-!-margin-bottom-0"
        labelClass="govuk-visually-hidden"/>
    </td>
  </tr>
</#macro>

<#macro standardDensityAndGasContentInfo>
  <h2 class="govuk-heading-s">
    Standard density
  </h2>
  <p class="govuk-body">
    Of the hydrocarbon component of the streams.
  </p>
  <h2 class="govuk-heading-s">
    Inert gas content
  </h2>
  <p class="govuk-body">
    Only inert gases obtained from the licenced fields should be reported within the consents system (e.g. N2 or CO2 from the reservoir). Those brought in as a utility provision should be removed from the figures reported within the field consents system.
  </p>
</#macro>
