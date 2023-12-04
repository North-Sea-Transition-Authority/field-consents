<#include '../layout/layout.ftl'>

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentTemplateSectionSummaryViews as documentTemplateSectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=documentTemplateSectionSummaryView.title()
            linkUrl="#${documentTemplateSectionSummaryView.title()}"
          />
        </#list>
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@defaultPageWithSubNavigationSubNav>

  <@defaultPageWithSubNavigationContent pageHeading=pageTitle>
    <#list documentTemplateSectionSummaryViews as documentTemplateSectionSummaryView>
      <h2 id="${documentTemplateSectionSummaryView.title()}" class="govuk-heading-l govuk-!-margin-bottom-2">
        ${documentTemplateSectionSummaryView.title()}
      </h2>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentTemplateSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
