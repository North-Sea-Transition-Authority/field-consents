<#include '../layout/layout.ftl'>

<#macro standardTaskList taskListSections showSectionNumber=true>
  <#list taskListSections as section>
    <#if showSectionNumber>
      <#local currentSectionNumber="${section?index + 1}"/>
    <#else>
      <#local currentSectionNumber=""/>
    </#if>
    <@fdsTaskList.taskList>
      <@fdsTaskList.taskListSection
        sectionNumber=currentSectionNumber
        sectionHeadingText=section.displayName()>
        <#list section.items() as item>
          <#local cannotStart=false/>
          <#if item.label().name() = 'BLOCKED'>
            <#local cannotStart=true/>
            <#local itemUrl=""/>
            <#local tagText=""/>
            <#local tagClass=""/>
          <#elseif item.label().name() = 'NOT_STARTED'>
            <#local itemUrl=springUrl(item.actionUrl())/>
            <#local tagText="Not started"/>
            <#local tagClass="govuk-tag--grey"/>
          <#elseif item.label().name() = 'IN_PROGRESS'>
            <#local itemUrl=springUrl(item.actionUrl())/>
            <#local tagText="In progress"/>
            <#local tagClass="govuk-tag--blue"/>
          <#else>
            <#local itemUrl=springUrl(item.actionUrl())/>
            <#local tagText=""/>
            <#local tagClass=""/>
          </#if>
          <@fdsTaskList.taskListItem
            itemText=item.displayName()
            itemUrl=itemUrl
            showTag=true
            completed=item.label().name() = 'COMPLETED'
            tagText=tagText
            tagClass=tagClass
            cannotStart=cannotStart
          />
        </#list>
      </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>
  </#list>
</#macro>
