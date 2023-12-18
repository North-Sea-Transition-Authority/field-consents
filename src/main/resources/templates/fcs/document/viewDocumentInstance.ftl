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
    <@fdsAction.link
      linkText="Preview document"
      linkUrl=springUrl(previewUrl)
      linkClass="govuk-button govuk-button--blue"
      role=true
    />

    <#list documentSectionSummaryViews as documentSectionSummaryView>
      <h2 id="${documentSectionSummaryView.title()}" class="govuk-heading-l govuk-!-margin-bottom-2">
        ${documentSectionSummaryView.title()}
      </h2>

     <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section before"
         linkAction=true
         linkActionUrl=springUrl(documentSectionSummaryView.addSectionBeforeUrl())
         linkActionScreenReaderText=documentSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section after"
         linkAction=true
         linkActionUrl=springUrl(documentSectionSummaryView.addSectionAfterUrl())
         linkActionScreenReaderText=documentSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add subsection"
         linkAction=true
         linkActionUrl=springUrl(documentSectionSummaryView.addSubsectionUrl())
         linkActionScreenReaderText=documentSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Edit"
         linkAction=true
         linkActionUrl=springUrl(documentSectionSummaryView.editUrl())
         linkActionScreenReaderText=documentSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Remove"
         linkAction=true
         linkActionUrl=springUrl(documentSectionSummaryView.removeUrl())
         linkActionScreenReaderText=documentSectionSummaryView.title()
       />
     </@fdsActionDropdown.actionDropdown>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
