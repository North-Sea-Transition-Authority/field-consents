<#include '../../layout/layout.ftl'>
<#import '../../dataitems/applicationDataItem.ftl' as applicationDataItemFtl>
<#import '../../../fds/utilities/utilities.ftl' as fdsUtil>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_PAGE_WIDTH
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <#list actions as action>
      <@fdsAction.button buttonText=action />
    </#list>
    <@fdsSearch.searchPage>
      <@fdsSearch.searchPageContent>
        <@fdsResultList.resultList resultCount=applicationDataItems?size>
          <#list applicationDataItems as dataItem>
            <@_selectableApplicationDataItem dataItem=dataItem path="form.selectedApplicationIds"/>
          </#list>
        </@fdsResultList.resultList>
      </@fdsSearch.searchPageContent>
    </@fdsSearch.searchPage>
  </@fdsForm.htmlForm>
</@defaultPage>

<#macro _selectableApplicationDataItem dataItem path inputHintText="" inputClass="" labelWrap=false>
  <@spring.bind path/>

  <#local id=fdsUtil.sanitiseId(spring.status.expression)>
  <#local name=fdsUtil.getSpringStatusExpression()>
  <#local hasError=fdsUtil.hasSpringStatusErrors()>

  <#local selectedApplicationIds = spring.stringStatusValue?has_content?then(spring.stringStatusValue?split(","), [])>
  <#local isSelected = selectedApplicationIds?seq_contains("${dataItem.applicationId()}")>

  <#local labelText=isSelected?then("de-select application ${dataItem.reference()}", "select application ${dataItem.reference()}")>

  <div style="display: flex; align-items: center;">
    <div class="govuk-checkboxes__item">
      <input
        class="govuk-checkboxes__input ${inputClass}"
        id="${id}"
        name="${name}"
        type="checkbox"
        value="${dataItem.applicationId()}"
        <#if isSelected>checked</#if>
        <#if inputHintText?has_content>aria-describedby="${id}-item-hint"</#if>
      />
      <label class="govuk-label govuk-checkboxes__label <#if labelWrap>govuk-checkboxes__label--wrap</#if>" for="${id}">
        <span class="govuk-visually-hidden">${labelText}</span>
      </label>
    </div>
    <div class="govuk-!-width-full">
      <@applicationDataItemFtl.applicationResultListItem dataItem=dataItem/>
    </div>
  </div>
</#macro>
