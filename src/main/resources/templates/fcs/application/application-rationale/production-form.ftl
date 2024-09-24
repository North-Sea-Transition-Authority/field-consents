<#include '../../layout/layout.ftl'>
<#include 'emission-and-oil-gas-maximum-details.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign heading="Application rationale"/>

<@defaultPage
pageHeading=heading
htmlTitle=heading
errorItems=errorList
backLinkUrl=springUrl(cancelUrl)
>
  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
      path="form.rationaleType"
      labelText="Is this application for an increase in production, a decrease in production or an extension?"
      fieldsetHeadingClass="govuk-fieldset__legend--m"
      hintText="An increase is where the maximum figure you are applying for is higher than that on this year’s consent. A decrease is where the minimum figure you are applying for is less than that on this year’s consent."
      hiddenContent=true>
      <#if oilAndGasMaximums?has_content>
        <@oilAndGasMaximumsSection oilAndGasMaximums/>
      </#if>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={increaseRadio: increaseRadio.displayName}>
        <@fdsTextarea.textarea
        labelText="Enter why you are requesting an increase."
        path="form.increaseComment.inputValue"
        nestingPath="form.rationaleType"
        />
      </@fdsRadio.radioItem>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={decreaseRadio: decreaseRadio.displayName}>
        <@fdsTextarea.textarea
        labelText="Enter why you are requesting a decrease."
        path="form.decreaseComment.inputValue"
        nestingPath="form.rationaleType"
        />
      </@fdsRadio.radioItem>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={extensionRadio: extensionRadio.displayName}>
        <@fdsTextarea.textarea
          labelText="Enter why you are requesting an extension."
          path="form.extensionComment.inputValue"
          nestingPath="form.rationaleType"
        />
      </@fdsRadio.radioItem>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={otherRadio: otherRadio.displayName}>
        <@fdsTextarea.textarea
          labelText="Enter why you have selected 'other'. This could be for administrative reasons. For example Operator change or COP."
          path="form.otherComment.inputValue"
          nestingPath="form.rationaleType"
        />
      </@fdsRadio.radioItem>
    </@fdsRadio.radioGroup>
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeading="At which location are the production activities?"
      legendHeadingClass="govuk-fieldset__legend--m"
      hintText="List all the physical location(s) of all production activities associated with this consent
                (e.g. wellhead platform location and host processing facility location).
                Note that this should be the location of the production equipment, not the source of the production.">
      <@fdsAddToList.addToList
        pathForList="form.productionLocationAssetKeys"
        pathForSelector="form.productionLocationAssetKeysSelector"
        restUrl=springUrl(productionLocationSearchUrl)
        alreadyAdded=productionLocations
        itemName="Production locations"
        selectorMinInputLength=2/>
    </@fdsFieldset.fieldset>
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeading="What is the host?"
      legendHeadingClass="govuk-fieldset__legend--m"
      hintText="The primary processing facility associated with this consent.
                For single entities this may be the same as the Production Location.">
      <@fdsSearchSelector.searchSelectorRest
        path="form.hostLocationAssetKey"
        restUrl=springUrl(hostLocationSearchUrl)
        labelText=""
        labelHeadingClass="govuk-label--s"
        preselectedItems={hostLocation.id(): hostLocation.text()}
        selectorMinInputLength=2/>
    </@fdsFieldset.fieldset>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
