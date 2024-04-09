<#include '../layout/layout.ftl'>

<#assign pageTitle = "Remove section"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle pageSize=PageSize.FULL_WIDTH>
  <@fdsForm.htmlForm>
    <@fdsSummaryList.summaryListCard headingText="Section details" summaryListId="section-details-summary-card-list">
      <@fdsSummaryList.summaryListRowNoAction keyText="Section title">
        ${documentSectionDto.title()}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Section text">
        <p class="govuk-body govuk-body__preserve-whitespace">${(documentSectionDto.content()!)?no_esc}</p>
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>

    <#if documentSectionDtoDescendants?has_content>
      <@fdsWarning.warning>
        Removing this section will also remove all the section's subsections.
      </@fdsWarning.warning>
    </#if>

    <@fdsAction.submitButtons
      primaryButtonText="Remove"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
      primaryButtonClass="govuk-button govuk-button--warning"
    />
  </@fdsForm.htmlForm>
</@defaultPage>
