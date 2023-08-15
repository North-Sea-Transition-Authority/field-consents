<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="applicationContext" type="java.util.List<uk.co.nstauthority.fieldconsents.application.ApplicationContextJson>" -->

<#macro applicationContextInfo applicationContext>
  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key=applicationContext.getPrimaryAssetPrompt() value=applicationContext.getPrimaryAssetName()/>
    <@fdsDataItems.dataValues key=applicationContext.getPrimaryOperatorPrompt() value=applicationContext.getPrimaryOperatorName()/>
  </@fdsDataItems.dataItem>
</#macro>
