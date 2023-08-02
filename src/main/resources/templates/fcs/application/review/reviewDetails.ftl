<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="technicalReviewSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView" -->

<#macro reviewDetails technicalReviewSummaryView>
    <@fdsSummaryList.summaryListCard headingText="Review details" summaryListId="review-details-summary-card-list">
        <@fdsSummaryList.summaryListRowNoAction keyText="Review deadline" >
            ${technicalReviewSummaryView.deadline()!""}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="Review note">
            ${technicalReviewSummaryView.note()!""}
        </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>
</#macro>
