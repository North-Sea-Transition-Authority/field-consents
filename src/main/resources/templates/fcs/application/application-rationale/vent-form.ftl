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
      labelText="Is this application for an increase or decrease?"
      fieldsetHeadingClass="govuk-fieldset__legend--m"
      hintText="An increase is where the figure you are applying for is higher than your current consent. A decrease is where the figure you are applying for is less than your current consent."
      hiddenContent=true>
      <#if emissionDailyAverage?has_content>
        <@emissionDailyAverageSection emissionDailyAverage/>
      </#if>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={increaseRadio: increaseRadio.displayName}>
        <@fdsTextarea.textarea
          labelText="Explain why you are requesting an increase. This could be that additional Wells are being drilled."
          path="form.increaseComment.inputValue"
          nestingPath="form.rationaleType"
        />
      </@fdsRadio.radioItem>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={decreaseRadio: decreaseRadio.displayName}/>
      <@fdsRadio.radioItem path="form.rationaleType" itemMap={noChangeRadio: noChangeRadio.displayName}/>
    </@fdsRadio.radioGroup>
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeading="Where does the venting take place?"
      legendHeadingClass="govuk-fieldset__legend--m"
      hintText="List all the physical location(s) of all venting activities associated with this consent.
                Note that this should be the location of the vent(s), not the source of the gas.">
      <@fdsAddToList.addToList
        pathForList="form.ventingLocationAssetKeys"
        pathForSelector="form.ventingLocationAssetKeysSelector"
        restUrl=springUrl(ventingLocationSearchUrl)
        alreadyAdded=ventingLocations
        itemName="Venting locations"/>
    </@fdsFieldset.fieldset>
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeading="What is the host?"
      legendHeadingClass="govuk-fieldset__legend--m"
      hintText="The primary processing facility associated with this consent.
                For single entities this may be the same as the Vent Location.">
      <@fdsSearchSelector.searchSelectorRest
        path="form.hostLocationAssetKey"
        restUrl=springUrl(hostLocationSearchUrl)
        labelText=""
        labelHeadingClass="govuk-label--s"
        preselectedItems={hostLocation.id(): hostLocation.text()}/>
    </@fdsFieldset.fieldset>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
