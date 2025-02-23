<!--
Component for items in a dropdown menu managed by the DropdownMenu component.
-->

<script setup lang="ts">
import { inject, ref } from 'vue';

import { closeDropdownInjectionKey } from './DropdownMenu.vue';

defineOptions({ inheritAttrs: false });

const props = defineProps<{
  text?: string;
  selected?: boolean;
}>();

const emit = defineEmits<{
  click: [event?: MouseEvent];
}>();

const closeDropdown = inject(closeDropdownInjectionKey)!;

const elem = ref<HTMLElement>();
const hasFocus = ref(false);
function setHasFocus(newValue: boolean): void {
  hasFocus.value = newValue;
}

function click(event?: MouseEvent): void {
  closeDropdown();
  emit('click', event);
}
</script>

<template>
  <li role="none">
    <a
      ref="elem"
      v-bind="$attrs"
      role="menuitem"
      :tabindex="hasFocus ? 0 : -1"
      :class="{ selected: props.selected }"
      @click="click"
      @keydown.enter.stop.prevent="click()"
      @mousemove.passive="elem?.focus()"
      @focusin.passive="setHasFocus(true)"
      @focusout.passive="setHasFocus(false)"
      ><slot>{{ props.text }}</slot></a
    >
  </li>
</template>
