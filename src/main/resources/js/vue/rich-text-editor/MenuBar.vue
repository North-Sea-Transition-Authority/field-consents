<template>
  <div class="rich-text-editor-menubar">
    <!-- undo/redo -->
    <menu-bar-group>
      <menu-bar-button label="Undo"
                       @click.prevent="editor?.chain().focus().undo().run()"
                       :disabled="!editor?.can().chain().focus().undo().run()">
        <icon-arrow-back-up :color="color" :stroke="stroke"/>
      </menu-bar-button>
      <menu-bar-button label="Redo"
                       @click.prevent="editor?.chain().focus().redo().run()"
                       :disabled="!editor?.can().chain().focus().redo().run()">
        <icon-arrow-forward-up :color="color" :stroke="stroke"/>
      </menu-bar-button>
    </menu-bar-group>

    <!-- formatting -->
    <menu-bar-group>
      <menu-bar-button label="Bold"
                       @click.prevent="editor?.chain().focus().toggleBold().run()"
                       :is-active="editor?.isActive('bold')">
        <icon-bold :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Italic"
                       @click.prevent="editor?.chain().focus().toggleItalic().run()"
                       :is-active="editor?.isActive('italic')">
        <icon-italic :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Underline"
                       @click.prevent="editor?.chain().focus().toggleUnderline().run()"
                       :is-active="editor?.isActive('underline')">
        <icon-underline :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Strikethrough" @click.prevent="editor?.chain().focus().toggleStrike().run()"
                       :is-active="editor?.isActive('strike')">
        <icon-strikethrough :color="color" :stroke="stroke"/>
      </menu-bar-button>
    </menu-bar-group>

    <!-- alignment -->
    <menu-bar-group>
      <menu-bar-button label="Left align"
                       @click.prevent="editor?.chain().focus().setTextAlign('left').run()"
                       :is-active="editor?.isActive({ textAlign: 'left' })">
        <icon-align-left :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Center align"
                       @click.prevent="editor?.chain().focus().setTextAlign('center').run()"
                       :is-active="editor?.isActive({ textAlign: 'center' })">
        <icon-align-center :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Right align"
                       @click.prevent="editor?.chain().focus().setTextAlign('right').run()"
                       :is-active="editor?.isActive({ textAlign: 'right' })">
        <icon-align-right :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Justify"
                       @click.prevent="editor?.chain().focus().setTextAlign('justify').run()"
                       :is-active="editor?.isActive({ textAlign: 'justify' })">
        <icon-align-justified :color="color" :stroke="stroke"/>
      </menu-bar-button>
    </menu-bar-group>

    <!-- lists -->
    <menu-bar-group>
      <menu-bar-button label="Bullet list"
                       @click.prevent="editor?.chain().focus().toggleBulletList().run()"
                       :is-active="editor?.isActive('bulletList')">
        <icon-list :color="color" :stroke="stroke"/>
      </menu-bar-button>

      <menu-bar-button label="Numbered list"
                       @click.prevent="editor?.chain().focus().toggleOrderedList().run()"
                       :is-active="editor?.isActive('orderedList')">
        <icon-list-numbers :color="color" :stroke="stroke"/>
      </menu-bar-button>
    </menu-bar-group>
  </div>
</template>

<script setup>
import MenuBarGroup from "./MenuBarGroup.vue";
import MenuBarButton from "./MenuBarButton.vue";
import {
  IconAlignCenter,
  IconAlignJustified,
  IconAlignLeft,
  IconAlignRight,
  IconArrowBackUp,
  IconArrowForwardUp,
  IconBold,
  IconItalic,
  IconList,
  IconListNumbers,
  IconStrikethrough,
  IconUnderline
} from "@tabler/icons-vue"

const color = "#000";
const stroke = 2;

const {editor} = defineProps(["editor"]);
</script>
