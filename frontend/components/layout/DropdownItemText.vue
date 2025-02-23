<!--
Component for non-clickable items in a dropdown menu managed by the DropdownMenu component.
-->

<script setup lang="ts">
defineOptions({ inheritAttrs: false });

const props = defineProps<{
  text?: string;
}>();
</script>

<template>
  <li role="menuitem" v-bind="$attrs" tabindex="-1">
    <slot>{{ props.text }}</slot>
  </li>
</template>
