<#include '../layout/layout.ftl'>

<#macro standardTaskList taskListSections>
  <#list taskListSections as section>
    <@fdsTaskList.taskList>
      <@fdsTaskList.taskListSection sectionHeadingText=section.displayName()>
        <#list section.items() as item>
          <#if item.label().name() = 'BLOCKED'>
            <#local itemUrl=""/>
            <#local tagText="Cannot start yet"/>
            <#local tagClass="govuk-tag--grey"/>
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
          />
        </#list>
      </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>
  </#list>
</#macro>