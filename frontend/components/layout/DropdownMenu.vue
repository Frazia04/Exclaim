<!--
Component for dropdown menus in the main header. Use together with the DropdownItem* component.
Inspired by: https://www.w3.org/WAI/ARIA/apg/patterns/menu-button/examples/menu-button-links/
-->

<script lang="ts">
import CollapsedIcon from '@material-symbols/svg-400/outlined/arrow_drop_down.svg';
import ExpandedIcon from '@material-symbols/svg-400/outlined/arrow_drop_up.svg';
import type { InjectionKey } from 'vue';

export const closeDropdownInjectionKey: InjectionKey<() => void> = Symbol(__DEV__ ? 'closeDropdownInjectionKey' : '');
</script>

<script setup lang="ts">
import { nextTick, onUnmounted, provide, shallowRef, watch } from 'vue';

defineOptions({ inheritAttrs: false });

defineProps<{
  id: string;
  text?: string;
}>();

provide(closeDropdownInjectionKey, closeDropdownAndFocusButton);

const expanded = shallowRef(false);
const elem = shallowRef<HTMLElement>();

function closeDropdownAndFocusButton(): void {
  expanded.value = false;
  (elem.value?.firstChild as HTMLElement | null | undefined)?.focus();
}

function onButtonClick(): void {
  // Toggle the expanded state
  if ((expanded.value = !expanded.value)) {
    // If we have just expanded, then wait for re-render and focus the first item
    void nextTick(focusSelectedOrFirstOrLastItem);
  }
}

// ------------------------------------------------------------------------------------------------
// Helpers to focus items in the dropdown

const itemSelector = 'a[role="menuitem"]';

function focusSelectedOrFirstOrLastItem(lastItem: boolean = false): void {
  const items = elem.value?.querySelectorAll<HTMLElement>(itemSelector);
  if (items?.length) {
    for (const item of items) {
      if (item.classList.contains('selected')) {
        item.focus();
        return;
      }
    }
    items[lastItem ? items.length - 1 : 0].focus();
  }
}

function focusSiblingItem(offset: number, currentElem: HTMLElement): void {
  const items = elem.value?.querySelectorAll<HTMLElement>(itemSelector);
  const numItems = items?.length;
  if (numItems) {
    let i = 0;
    while (i < numItems && !items[i].contains(currentElem)) {
      i++;
    }
    // avoid negative indices, e.g. ((-1 % 3) + 3) % 3 = (-1 + 3) % 3 = 2
    items[(((i + offset) % numItems) + numItems) % numItems].focus();
  }
}

// ------------------------------------------------------------------------------------------------

function onButtonKeydown(event: KeyboardEvent): void {
  let focusLastItem = false;
  switch (event.key) {
    case ' ':
    case 'Enter':
    case 'ArrowDown':
      break;

    case 'ArrowUp':
      focusLastItem = true;
      break;

    default:
      return;
  }

  expanded.value = true;
  void nextTick(() => focusSelectedOrFirstOrLastItem(focusLastItem));

  event.stopPropagation();
  event.preventDefault();
}

function onItemsKeydown(event: KeyboardEvent): void {
  let siblingOffset: number;
  switch (event.key) {
    case 'ArrowDown':
      siblingOffset = 1;
      break;

    case 'ArrowUp':
      siblingOffset = -1;
      break;

    default:
      return;
  }

  const eventTarget = event.target as HTMLElement | null;
  if (eventTarget) {
    focusSiblingItem(siblingOffset, eventTarget);
  }

  event.stopPropagation();
  event.preventDefault();
}

// ------------------------------------------------------------------------------------------------
// When the dropdown is expanded, install event listeners to close it when performing any
// interaction outside the dropdown or pressing the escape key

type EventListenerParameters<T extends keyof WindowEventMap> = [
  type: T,
  listener: (event: WindowEventMap[T]) => void,
  options?: boolean | AddEventListenerOptions,
];
const eventListeners = [
  ['keydown', onBackgroundEscape, { capture: true }] satisfies EventListenerParameters<'keydown'>,
  ['keydown', onBackgroundAction, { capture: true, passive: true }] satisfies EventListenerParameters<'keydown'>,
  ['mousedown', onBackgroundAction, { capture: true, passive: true }] satisfies EventListenerParameters<'mousedown'>,
  ['focusin', onBackgroundAction, { capture: true, passive: true }] satisfies EventListenerParameters<'focusin'>,
] as EventListenerParameters<keyof WindowEventMap>[];

function onBackgroundEscape(event: KeyboardEvent): void {
  if (event.key === 'Escape') {
    closeDropdownAndFocusButton();
    event.stopPropagation();
    event.preventDefault();
  }
}

function onBackgroundAction(event: KeyboardEvent | MouseEvent | FocusEvent): void {
  if (!elem.value?.contains(event.target as Node | null)) {
    expanded.value = false;
  }
}

function addEventListeners(): void {
  for (const params of eventListeners) {
    window.addEventListener(...params);
  }
}

function removeEventListeners(): void {
  for (const params of eventListeners) {
    window.removeEventListener(...params);
  }
}

watch(
  expanded,
  (expanded) => {
    if (expanded) {
      addEventListeners();
    } else {
      removeEventListeners();
    }
  },
  { flush: 'sync' },
);

// Make sure to cleanup event listeners when this component gets unmounted
onUnmounted(removeEventListeners);
</script>

<template>
  <li :id="id" ref="elem" class="dropdown">
    <button
      v-bind="$attrs"
      :id="id + '-toggle'"
      type="button"
      aria-haspopup="true"
      :aria-controls="id + '-dropdown'"
      :aria-expanded="expanded"
      class="wrapper"
      @click.stop.prevent="onButtonClick"
      @keydown="onButtonKeydown"
    >
      <slot name="button-content">{{ text }}</slot>
      <ExpandedIcon v-if="expanded" />
      <CollapsedIcon v-else />
    </button>
    <ul :id="id + '-dropdown'" role="none" @keydown="onItemsKeydown">
      <slot />
    </ul>
  </li>
</template>
