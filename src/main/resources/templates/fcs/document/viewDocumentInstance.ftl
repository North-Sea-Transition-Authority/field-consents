<#include '../layout/layout.ftl'>

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=documentInstanceSectionSummaryView.title()
            linkUrl="#${documentInstanceSectionSummaryView.title()}"
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

    <@fdsAction.link
      linkText="Reload document"
      linkUrl=springUrl(reloadUrl)
      linkClass="govuk-button govuk-button--secondary"
      role=true
    />

    <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
      <h2 id="${documentInstanceSectionSummaryView.title()}" class="govuk-heading-l govuk-!-margin-bottom-2">
        ${documentInstanceSectionSummaryView.title()}
      </h2>

     <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section before"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSectionBeforeUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section after"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSectionAfterUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add subsection"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSubsectionUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Edit"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.editUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.title()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Remove"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.removeUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.title()
       />
     </@fdsActionDropdown.actionDropdown>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentInstanceSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
