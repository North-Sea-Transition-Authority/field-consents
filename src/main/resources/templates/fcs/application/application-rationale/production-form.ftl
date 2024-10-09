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
    <@rationaleCategoryInfo/>
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeading="At which location are the production activities?"
      legendHeadingClass="govuk-fieldset__legend--m"
      hintText="List all the physical location(s) of all production activities associated with this consent
                (e.g. wellhead platform location and host processing facility location).
                Note that this should be the location of all production processing equipment,
                not just the field that is the source of the production.">
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

<#macro rationaleCategoryInfo>
  <@fdsDetails.summaryDetails summaryTitle="I do not know if my application is for an increase, decrease or extension">
    <@rationaleInfo/>
  </@fdsDetails.summaryDetails>
</#macro>

<#macro rationaleInfo>
  <h3 class="govuk-heading-s">
    Increase
  </h3>
  <p class="govuk-body">
    Compared to the previous year “increase” should be selected where:
    <ul>
      <li>
        there is an increase in the averaged oil or;
      </li>
      <li>
        there is an increase in the averaged gas profile or;
      </li>
      <li>
        any of the years show an increase over the current year's consented volumes or;
      </li>
      <li>
        there is no previous consent as the application is related to an FDP
      </li>
    </ul>
  </p>
  <h3 class="govuk-heading-s">
    Decrease
  </h3>
  <p class="govuk-body">
    Compared to the previous year “decrease” should be selected where:
    <ul>
      <li>
        there is a decrease in the average oil or;
      </li>
      <li>
        there is a decrease in the averaged gas profile or;
      </li>
      <li>
        any of the years show a decrease over the current year's consented volumes
      </li>
    </ul>
  </p>
  <h3 class="govuk-heading-s">
    Extension
  </h3>
  <p class="govuk-body">
    Should only be selected with prior agreement of the NSTA and will typically relate to unique circumstances around end of field life.
  </p>
  <h3 class="govuk-heading-s">
    Other
  </h3>
  <p class="govuk-body">
    Should be selected when this application does not result in an increase, decrease, or extension in production, for example when there is a change of operator.
  </p>
</#macro>
