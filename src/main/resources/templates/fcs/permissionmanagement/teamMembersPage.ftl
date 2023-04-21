<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamRoles" type="java.util.List<uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole>" -->
<#-- @ftlvariable name="teamMembers" type="java.util.List<uk.co.nstauthority.fieldconsents.teams.TeamMemberView>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="canRemoveUsers" type="java.lang.Boolean" -->
<#-- @ftlvariable name="canEditUsers" type="java.lang.Boolean" -->
<#-- @ftlvariable name="addTeamMemberUrl" type="java.lang.String" -->
<#include '../layout/layout.ftl'>
<#import '_teamMembers.ftl' as teamMembersMacro>
<#import '_roleDescriptions.ftl' as roleDescriptions>

<#assign pageTitle=teamName/>

<#if backLinkUrl?has_content>
  <#assign backLinkSpringUrl=springUrl(backLinkUrl)/>
<#else>
  <#assign backLinkSpringUrl=""/>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=backLinkSpringUrl
>

  <@roleDescriptions.roleDescriptions roles=teamRoles/>

  <#if addTeamMemberUrl?has_content>
    <@fdsAction.link
      linkText="Add user"
      linkUrl=springUrl(addTeamMemberUrl)
      linkClass="govuk-button govuk-button--secondary"
      role=true
    />
  </#if>

  <#if teamMembers?has_content>
      <@teamMembersMacro.teamMembers name=teamName members=teamMembers canRemoveUsers=canRemoveUsers!false canEditUsers=canEditUsers!false/>
  <#else>
    <@fdsInsetText.insetText>
      ${teamName} has no members.
    </@fdsInsetText.insetText>
  </#if>

</@defaultPage>