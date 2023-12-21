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

     <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section before"
         linkAction=true
         linkActionUrl=springUrl(documentTemplateSectionSummaryView.addSectionBeforeUrl())
         linkActionScreenReaderText=documentTemplateSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section after"
         linkAction=true
         linkActionUrl=springUrl(documentTemplateSectionSummaryView.addSectionAfterUrl())
         linkActionScreenReaderText=documentTemplateSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add subsection"
         linkAction=true
         linkActionUrl=springUrl(documentTemplateSectionSummaryView.addSubsectionUrl())
         linkActionScreenReaderText=documentTemplateSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Edit"
         linkAction=true
         linkActionUrl=springUrl(documentTemplateSectionSummaryView.editUrl())
         linkActionScreenReaderText=documentTemplateSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Remove"
         linkAction=true
         linkActionUrl=springUrl(documentTemplateSectionSummaryView.removeUrl())
         linkActionScreenReaderText=documentTemplateSectionSummaryView.title()
       />
     </@fdsActionDropdown.actionDropdown>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentTemplateSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
