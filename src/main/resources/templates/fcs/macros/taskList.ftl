<#include '../layout/layout.ftl'>

<#macro standardTaskList taskListSections>
  <#list taskListSections as section>
    <@fdsTaskList.taskList>
      <@fdsTaskList.taskListSection sectionHeadingText=section.displayName()>
        <#list section.items() as item>
          <#if item.label().name() = 'BLOCKED'>
            <#assign itemUrl=""/>
            <#assign tagText="Cannot start yet"/>
            <#assign tagClass="govuk-tag--grey"/>
          <#elseif item.label().name() = 'NOT_STARTED'>
            <#assign itemUrl=springUrl(item.actionUrl())/>
            <#assign tagText="Not started"/>
            <#assign tagClass="govuk-tag--grey"/>
          <#elseif item.label().name() = 'IN_PROGRESS'>
            <#assign itemUrl=springUrl(item.actionUrl())/>
            <#assign tagText="In progress"/>
            <#assign tagClass="govuk-tag--blue"/>
          <#else>
            <#assign itemUrl=springUrl(item.actionUrl())/>
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