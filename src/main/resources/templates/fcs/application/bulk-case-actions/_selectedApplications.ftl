<#include '../../layout/layout.ftl'>
<#import '../../../fds/utilities/utilities.ftl' as fdsUtil>
<#import '../../dataitems/applicationDataItem.ftl' as applicationDataItemFtl>

<#macro summaryList applicationDataItems captionHeadingFunction>
  <@fdsResultList.resultList resultCount=applicationDataItems?size resultCountSuffix="selected application">
    <#list applicationDataItems as applicationDataItem>
      <@fdsResultList.resultListItem
        linkHeadingText=applicationDataItem.reference()
        linkHeadingUrl=applicationDataItem.url()
        captionHeadingText=captionHeadingFunction.apply(applicationDataItem)
        />
    </#list>
  </@fdsResultList.resultList>
</#macro>
