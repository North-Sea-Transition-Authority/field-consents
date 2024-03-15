<#macro richTextEditor path labelText="Text">
  <@spring.bind path/>

  <#local name=spring.status.expression>
  <#local value=spring.stringStatusValue>

  <div class="govuk-form-group" data-module="rich-text-editor">
    <label class="govuk-label">${labelText}</label>
    <input data-rich-text-editor="editor-output" class="rich-text-editor-output" name="${name}" value="${value}"/>
    <div data-rich-text-editor="editor-input" class="rich-text-editor-input">
      ${value}
    </div>
    <div data-rich-text-editor="editor"></div>
  </div>
</#macro>
