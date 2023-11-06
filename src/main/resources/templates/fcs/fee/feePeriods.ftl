<#include '../layout/layout.ftl'>

<#assign pageTitle = "Fee periods"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
>
  <@fdsResultList.resultList>
    <#list feePeriodSummaryViews as feePeriodSummaryView>
      <#assign tag>
        <@fdsResultList.resultListTag
          tagText="${feePeriodSummaryView.status().getTagText()}"
          tagClass="${feePeriodSummaryView.status().getTagClass()}"
        />
      </#assign>
      <@fdsResultList.resultListItem
        linkHeadingUrl=springUrl(feePeriodSummaryView.viewUrl())
        linkHeadingText="${feePeriodSummaryView.title()}"
        itemTag=tag
      >
        <@fdsResultList.resultListDataItem>
          <@fdsResultList.resultListDataValue key="Start date" value="${feePeriodSummaryView.formattedStartDate()}"/>
          <#if feePeriodSummaryView.formattedEndDate()??>
            <@fdsResultList.resultListDataValue key="End date" value="${feePeriodSummaryView.formattedEndDate()}"/>
          </#if>
        </@fdsResultList.resultListDataItem>
        <#if feePeriodSummaryView.editUrl()??>
          <#assign editLink>
            <@fdsAction.link
              linkText="Edit"
              linkClass="govuk-button govuk-!-margin-bottom-0 govuk-button--secondary"
              linkUrl=springUrl(feePeriodSummaryView.editUrl())
              role=true
            />
          </#assign>
          <@fdsResultList.resultListDataItem>
            <@fdsResultList.resultListDataValue key=" " value=editLink/>
          </@fdsResultList.resultListDataItem>
        </#if>
      </@fdsResultList.resultListItem>
    </#list>
  </@fdsResultList.resultList>

  <@fdsAction.link
    linkText="Create new fee period"
    linkClass="govuk-button"
    linkUrl=springUrl(createFeePeriodUrl)
    role=true
  />
</@defaultPage>
