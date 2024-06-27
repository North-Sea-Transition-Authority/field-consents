<#include '../../../layout/layout.ftl'>

<#macro tab caseHistoryEvents>
<@fdsTimeline.timeline timelineClass="fds-timeline--left-padding">
  <@fdsTimeline.timelineSection sectionHeading="">
    <#list caseHistoryEvents as item>
      <#if item?counter == caseHistoryEvents?size>
        <#local stampClass="fds-timeline__time-stamp--no-border" />
      <#else>
        <#local stampClass = "fds-timeline__time-stamp" />
      </#if>
      <#assign headingWithScreenReaderText>
        ${item.headerText}
      </#assign>
      <@fdsTimeline.timelineTimeStamp
        nodeNumber="${caseHistoryEvents?size - item?index}"
        timeStampHeading=headingWithScreenReaderText
        timeStampHeadingSize="h2"
        timeStampHeadingHint=item.eventDateTimeText
        timeStampClass=stampClass>
        <@fdsTimeline.timelineEvent>
          <@fdsSummaryList.summaryListWrapper
            headingText=""
            headingClass="govuk-!-display-none"
            summaryListId="summary-list-${item?counter}">
            <@fdsSummaryList.summaryList>
              <@fdsSummaryList.summaryListRowNoAction keyText="Application version">
                ${item.applicationVersion}
              </@fdsSummaryList.summaryListRowNoAction>

              <#if item.mainUserInvolvedFullName?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText=item.mainUserInvolvedLabel>
                  ${item.mainUserInvolvedFullName}
                </@fdsSummaryList.summaryListRowNoAction>
              </#if>

              <#if item.otherUserInvolvedFullName?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText=item.otherUserInvolvedLabel>
                  ${item.otherUserInvolvedFullName}
                </@fdsSummaryList.summaryListRowNoAction>
              </#if>

              <#if item.eventText?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText=item.eventTextLabel>
                  <@multiLineText.multiLineText contentText=item.eventText/>
                </@fdsSummaryList.summaryListRowNoAction>
              </#if>

              <#if item.summaryFileViews?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Uploaded files">
                  <ul class="govuk-list">
                    <#list item.summaryFileViews as summaryFileView>
                      <li>
                        <@fdsAction.link
                          linkUrl=springUrl(summaryFileView.downloadUrl())
                          linkText=summaryFileView.filename()/>
                          <#if summaryFileView.description()?has_content>
                            <div>
                              <@multiLineText.multiLineText contentText=summaryFileView.description()/>
                            </div>
                          </#if>
                      </li>
                    </#list>
                  </ul>
                </@fdsSummaryList.summaryListRowNoAction>
              </#if>
            </@fdsSummaryList.summaryList>
          </@fdsSummaryList.summaryListWrapper>
        </@fdsTimeline.timelineEvent>
      </@fdsTimeline.timelineTimeStamp>
    </#list>
  </@fdsTimeline.timelineSection>
</@fdsTimeline.timeline>
</#macro>
