<#include '../layout/layout.ftl'>

<#macro standardTaskList taskListSections>
  <#list taskListSections as section>
    <@fdsTaskList.taskList>
      <@fdsTaskList.taskListSection sectionHeadingText=section.displayName()>
        <#list section.items() as item>
          <@fdsTaskList.taskListItem
            itemText=item.displayName()
            itemUrl=springUrl(item.actionUrl())
            showTag=true
            completed=item.label().name() = 'COMPLETED'
            useNotCompletedLabels=true
          />
        </#list>
      </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>
  </#list>
</#macro>