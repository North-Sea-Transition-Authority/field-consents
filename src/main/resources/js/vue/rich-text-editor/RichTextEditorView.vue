<template>
  <div class="rich-text-editor">
    <menu-bar :editor="editor"/>
    <editor-content :editor="editor"/>
  </div>
</template>

<script setup>
import {onBeforeUnmount} from "vue";
import {EditorContent, useEditor} from "@tiptap/vue-3";
import {Document} from "@tiptap/extension-document";
import {Paragraph} from "@tiptap/extension-paragraph";
import {Text} from "@tiptap/extension-text";
import {History} from "@tiptap/extension-history";
import {Bold} from "@tiptap/extension-bold";
import {Italic} from "@tiptap/extension-italic";
import {Underline} from "@tiptap/extension-underline";
import {Strike} from "@tiptap/extension-strike";
import {TextAlign} from "@tiptap/extension-text-align";
import {BulletList} from "@tiptap/extension-bullet-list";
import {OrderedList} from "@tiptap/extension-ordered-list";
import {ListItem} from "@tiptap/extension-list-item";
import MenuBar from "./MenuBar.vue";

const {editorInput, editorOutput} = defineProps(["editorInput", "editorOutput"]);

const editor = useEditor({
  content: editorInput,
  extensions: [
    Document,
    Paragraph,
    Text,
    History,
    Bold,
    Italic,
    Underline,
    Strike,
    TextAlign.configure({
      types: ["paragraph"],
    }),
    BulletList,
    OrderedList,
    ListItem
  ],
  parseOptions: {
     // The following must be set or `&nbsp;` will be inserted on lines which end with whitespace. When used with openhtmltopdf this breaks PDF rendering
    preserveWhitespace: true
  },
  onUpdate: ({editor}) => editorOutput.setAttribute("value", editor.getHTML()),
});

onBeforeUnmount(() => this.editor.destroy());
</script>
