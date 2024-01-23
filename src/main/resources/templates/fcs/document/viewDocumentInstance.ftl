<#include '../layout/layout.ftl'>

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=documentInstanceSectionSummaryView.titleWithSectionNumber()
            linkUrl="#${documentInstanceSectionSummaryView.titleWithSectionNumber()}"
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
      openInNewTab=true
    />

    <@fdsAction.link
      linkText="Reload document"
      linkUrl=springUrl(reloadUrl)
      linkClass="govuk-button govuk-button--secondary"
      role=true
    />

    <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
      <h2 id="${documentInstanceSectionSummaryView.titleWithSectionNumber()}" class="govuk-heading-l govuk-!-margin-bottom-2">
        ${documentInstanceSectionSummaryView.titleWithSectionNumber()}
      </h2>

     <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section before"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSectionBeforeUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add section after"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSectionAfterUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Add subsection"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.addSubsectionUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Edit"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.editUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
       />

       <@fdsActionDropdown.actionDropdownItem
         actionText="Remove"
         linkAction=true
         linkActionUrl=springUrl(documentInstanceSectionSummaryView.removeUrl())
         linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
       />
     </@fdsActionDropdown.actionDropdown>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentInstanceSectionSummaryView.content()!?no_esc}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
