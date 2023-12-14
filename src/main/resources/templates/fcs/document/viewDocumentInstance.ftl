<#include '../layout/layout.ftl'>

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentSectionSummaryViews as documentSectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=documentSectionSummaryView.title()
            linkUrl="#${documentSectionSummaryView.title()}"
          />
        </#list>
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@defaultPageWithSubNavigationSubNav>

  <@defaultPageWithSubNavigationContent pageHeading=pageTitle>
    <#list documentSectionSummaryViews as documentSectionSummaryView>
      <h2 id="${documentSectionSummaryView.title()}" class="govuk-heading-l govuk-!-margin-bottom-2">
        ${documentSectionSummaryView.title()}
      </h2>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
