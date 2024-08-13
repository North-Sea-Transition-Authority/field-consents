<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Consent duration"/>

<#if applicationIsRevision>
  <#assign pageHeading=pageTitle>
<#else>
  <#assign pageHeading="">
</#if>

<@defaultPage htmlTitle=pageTitle pageHeading=pageHeading errorItems=errorList>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <#if !applicationIsRevision>

      <@fdsRadio.radioGroup path="form.consentLengthType" hiddenContent=true labelText="Select the period of the consent you are applying for" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l">
      <@fdsInsetText.insetText>
        The consent length will determine the data you need to provide in the following sections of the application
      </@fdsInsetText.insetText>
      <#assign isFirstItem = true/>
      <#list consentTypes as option, displayText>
        <@fdsRadio.radioItem path="form.consentLengthType" itemMap={option: displayText} isFirstItem=isFirstItem>
          <#if option == "SHORT_TERM">
            <@fdsDateInput.dateInput
              labelText="Start date"
              dayPath="form.shortTermStartDate.dayInput.inputValue" monthPath="form.shortTermStartDate.monthInput.inputValue" yearPath="form.shortTermStartDate.yearInput.inputValue"
              formId="form.shortTermStartDate"
              nestingPath="form.consentLengthType"/>
            <@fdsDateInput.dateInput
              labelText="End date"
              dayPath="form.shortTermEndDate.dayInput.inputValue" monthPath="form.shortTermEndDate.monthInput.inputValue" yearPath="form.shortTermEndDate.yearInput.inputValue"
              formId="form.shortTermEndDate"
              nestingPath="form.consentLengthType"/>
            <#elseif option == "ANNUAL">
              <@fdsRadio.radioGroup path="form.annualConsentYear.inputValue" hiddenContent=true labelText="Year" nestingPath="form.consentLengthType">
                <#list annualConsentYears as annualYear, annualYearText>
                  <@fdsRadio.radioItem path="form.annualConsentYear.inputValue" itemMap={annualYear: annualYearText}/>
                </#list>
              </@fdsRadio.radioGroup>
            <#else>
                <@fdsSelect.select path="form.longTermStartYear.inputValue" options=longTermStartYears labelText="Start year" nestingPath="form.consentLengthType"/>
            <@fdsTextInput.textInput path="form.longTermEndYear.inputValue" labelText="End year" nestingPath="form.consentLengthType" labelClass="govuk-label--s" inputClass="govuk-input--width-5"/>
          </#if>
        </@fdsRadio.radioItem>
        <#assign isFirstItem = false/>
      </#list>
      </@fdsRadio.radioGroup>

    <#else>

      <#if consentLengthView.consentLengthType() == "SHORT_TERM">
        <@fdsInsetText.insetText>
          You cannot change the start date when revising a short term consent.
        </@fdsInsetText.insetText>

        <@fdsDataItems.dataItem>
          <@fdsDataItems.dataValues key="Start date" value=consentLengthView.formattedShortTermStartDate() />
        </@fdsDataItems.dataItem>

        <@fdsDateInput.dateInput
          labelText="End date"
          dayPath="form.shortTermEndDate.dayInput.inputValue" monthPath="form.shortTermEndDate.monthInput.inputValue" yearPath="form.shortTermEndDate.yearInput.inputValue"
          formId="form.shortTermEndDate"
        />
      <#elseif consentLengthView.consentLengthType() == "ANNUAL">
        <@fdsInsetText.insetText>
          You cannot change the year when revising an annual consent.
        </@fdsInsetText.insetText>

        <@fdsDataItems.dataItem>
          <@fdsDataItems.dataValues key="Year" value=consentLengthView.annualConsentYear() />
        </@fdsDataItems.dataItem>
      <#elseif consentLengthView.consentLengthType() == "LONG_TERM">
        <@fdsInsetText.insetText>
          You cannot change the start year when revising a long term consent.
        </@fdsInsetText.insetText>

        <@fdsDataItems.dataItem>
          <@fdsDataItems.dataValues key="Start year" value=consentLengthView.longTermStartYear() />
        </@fdsDataItems.dataItem>

        <@fdsTextInput.textInput path="form.longTermEndYear.inputValue" labelText="End year" labelClass="govuk-label--s" inputClass="govuk-input--width-5"/>
      </#if>
    </#if>

    <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl="${springUrl(cancelUrl)}" primaryButtonText="Save and complete" secondaryLinkText="Cancel"/>
  </@fdsForm.htmlForm>
</@defaultPage>
