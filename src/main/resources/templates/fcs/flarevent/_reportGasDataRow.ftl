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