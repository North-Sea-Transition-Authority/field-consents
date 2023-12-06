<#include '../layout/layout.ftl'>

<#assign pageTitle = "Remove section"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <@fdsForm.htmlForm>
    <@fdsSummaryList.summaryListCard headingText="Section details" summaryListId="section-details-summary-card-list">
      <@fdsSummaryList.summaryListRowNoAction keyText="Title">
        ${documentTemplateSectionDto.title()}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Text">
        <p class="govuk-body govuk-body__preserve-whitespace">${documentTemplateSectionDto.content()!}</p>
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>

    <#if documentTemplateSectionDto.descendants()?has_content>
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
