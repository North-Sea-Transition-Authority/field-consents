<#include '../../../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  backLinkWithBrowserBack=true>
  <@fdsForm.htmlForm>
    <@fdsDateInput.dateInput
      labelText="Consent start date"
      formId="form.consentStartDate"
      dayPath="form.consentStartDate.dayInput.inputValue"
      monthPath="form.consentStartDate.monthInput.inputValue"
      yearPath="form.consentStartDate.yearInput.inputValue"/>
    <@fdsDateInput.dateInput
      labelText="Consent end date"
      formId="form.consentEndDate"
      dayPath="form.consentEndDate.dayInput.inputValue"
      monthPath="form.consentEndDate.monthInput.inputValue"
      yearPath="form.consentEndDate.yearInput.inputValue"/>
    <@fdsAction.button buttonText="Save"/>
  </@fdsForm.htmlForm>
</@defaultPage>
