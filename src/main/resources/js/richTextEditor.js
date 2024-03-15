import {createApp} from "vue";
import RichTextEditorView from "./vue/rich-text-editor/RichTextEditorView.vue";

export default class RichTextEditor {

  constructor(element) {
    const editorInput = this.getEditorInput(element);
    const editorOutput = this.getEditorOutput(element);
    const editor = element.querySelector(`[data-rich-text-editor="editor"]`)

    createApp(RichTextEditorView, {editorInput, editorOutput}).mount(editor);
  }

  getEditorInput(element) {
    const editorInput = element.querySelector(`[data-rich-text-editor="editor-input"]`);
    const textContent = editorInput.textContent;
    editorInput.remove();

    return textContent;
  }

  getEditorOutput(element) {
    return element.querySelector(`[data-rich-text-editor="editor-output"]`);
  }

}
