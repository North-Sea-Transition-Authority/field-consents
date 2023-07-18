<#include '../../layout/layout.ftl'>

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
      <@fdsTimeline.timelineTimeStamp timeStampHeading=headingWithScreenReaderText nodeNumber=" " timeStampClass=stampClass>
        <@fdsTimeline.timelineEvent>
          <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key=item.eventDateTimeLabel value=item.eventDateTimeText/>
            <@fdsDataItems.dataValues key=item.mainUserInvolvedLabel value=item.mainUserInvolvedFullName/>
            <#if item.otherUserInvolvedFullName?has_content>
              <@fdsDataItems.dataValues key=item.otherUserInvolvedLabel value=item.otherUserInvolvedFullName/>
            </#if>
            <@fdsDataItems.dataValues key="Application version" value=item.applicationVersion/>
          </@fdsDataItems.dataItem>
          <#if item.eventText?has_content>
            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
              <@fdsDataItems.dataValues key=item.eventTextLabel value=item.eventText/>
            </@fdsDataItems.dataItem>
          </#if>
        </@fdsTimeline.timelineEvent>
      </@fdsTimeline.timelineTimeStamp>
    </#list>
  </@fdsTimeline.timelineSection>
</@fdsTimeline.timeline>
</#macro>
