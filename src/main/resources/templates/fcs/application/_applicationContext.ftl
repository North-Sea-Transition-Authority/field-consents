<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="applicationContext" type="java.util.List<uk.co.nstauthority.fieldconsents.application.ApplicationContext>" -->

<#macro applicationContextInfo applicationContext>
  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key=applicationContext.getPrimaryAssetPrompt() value=applicationContext.primaryAsset().getName()/>
    <@fdsDataItems.dataValues key=applicationContext.getStatusPrompt() value=applicationContext.applicationVersionStatus()/>
    <@fdsDataItems.dataValues key=applicationContext.getConsentDurationPrompt() value=applicationContext.consentDuration()!""/>
    <@fdsDataItems.dataValues key=applicationContext.getStartingYearPrompt() value=applicationContext.consentStartYear()!""/>
  </@fdsDataItems.dataItem>

  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key=applicationContext.getPrimaryOperatorPrompt() value=applicationContext.primaryOperator()/>
    <@fdsDataItems.dataValues key=applicationContext.getAssetOperatorsPrompt() value=applicationContext.getCommaSeparatedAssetOperators()/>
  </@fdsDataItems.dataItem>

  <#assign hasAdditionalFields = applicationContext.additionalFields()?has_content/>
  <#assign hasLicences = applicationContext.licences()?has_content/>
  <#if hasAdditionalFields || hasLicences>
    <@fdsDataItems.dataItem>
      <#if hasAdditionalFields>
        <@fdsDataItems.dataValues key=applicationContext.getAdditionalFieldsPrompt() value=applicationContext.getCommaSeparatedAdditionalFields()!""/>
      </#if>
      <#if hasLicences>
        <@fdsDataItems.dataValues key=applicationContext.getLicencesPrompt() value=applicationContext.getCommaSeparatedLicences()!""/>
      </#if>
    </@fdsDataItems.dataItem>
  </#if>
</#macro>
