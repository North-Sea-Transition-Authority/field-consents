<#include '../../layout/layout.ftl'>
<#import '../../../fds/utilities/utilities.ftl' as fdsUtil>
<#import '../../dataitems/applicationDataItem.ftl' as applicationDataItemFtl>

<#macro summaryList applicationDataItemViews captionHeadingFunction>
  <@fdsResultList.resultList resultCount=applicationDataItemViews?size resultCountSuffix="selected application">
    <#list applicationDataItemViews as applicationDataItemView>
      <@fdsResultList.resultListItem
        linkHeadingText=applicationDataItemView.reference()
        linkHeadingUrl=springUrl(applicationDataItemView.url())
        captionHeadingText=captionHeadingFunction.apply(applicationDataItemView)
        />
    </#list>
  </@fdsResultList.resultList>
</#macro>
