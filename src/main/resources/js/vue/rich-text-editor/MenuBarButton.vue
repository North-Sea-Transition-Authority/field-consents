<template>
  <button
      :class="['rich-text-editor-menubar-button', isActive && 'rich-text-editor-menubar-button__active']"
      :aria-label="label"
  >
    <slot/>
  </button>
</template>

<script setup>
const {label, isActive} = defineProps(["label", "isActive"])
</script>
