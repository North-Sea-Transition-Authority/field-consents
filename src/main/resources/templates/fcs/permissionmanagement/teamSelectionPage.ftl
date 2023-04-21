<#-- @ftlvariable name="allTeams" type="java.util.List<uk.co.nstauthority.fieldconsents.teams.TeamView>" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="industryNewTeamFormUrl" type="java.lang.String" -->
<#include '../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <#if industryNewTeamFormUrl?has_content>
    <@fdsAction.link linkText="Create industry team" linkClass="govuk-button" linkUrl=springUrl(industryNewTeamFormUrl)/>
  </#if>

  <@fdsResultList.resultList resultCount=allTeams?size resultCountSuffix="team">
    <#list allTeams as team>
      <@fdsResultList.resultListItem
        captionHeadingText=team.teamType().getDisplayText()
        linkHeadingUrl=springUrl(team.teamUrl())
        linkHeadingText=team.displayName()/>
    </#list>
  </@fdsResultList.resultList>

</@defaultPage>