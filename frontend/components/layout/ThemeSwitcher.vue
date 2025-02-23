<script setup lang="ts">
import DarkIcon from '@material-symbols/svg-400/outlined/dark_mode.svg';
import LightIcon from '@material-symbols/svg-400/outlined/light_mode.svg';

import { t } from '../../i18n';
import { type Theme, theme as currentTheme } from '../../store/theme';
import DropdownItemLink from './DropdownItemLink.vue';
import DropdownMenu from './DropdownMenu.vue';

const themes = [
  ['light', LightIcon],
  ['dark', DarkIcon],
] as const;

function setTheme(newTheme: Theme): void {
  currentTheme.value = newTheme;
}
</script>

<template>
  <DropdownMenu id="theme-switcher" :aria-label="`${t('theme.switch')} (${t(`theme.${currentTheme}`)})`">
    <template #button-content
      ><component :is="ThemeIcon" v-for="[theme, ThemeIcon] of themes" :key="theme" :class="`theme-${theme}-only`"
    /></template>
    <DropdownItemLink :selected="currentTheme === 'auto'" @click="setTheme('auto')"
      ><component
        :is="ThemeIcon"
        v-for="[theme, ThemeIcon] of themes"
        :key="theme"
        :class="`auto-theme-${theme}-only`"
      />
      {{ t('theme.auto') }}</DropdownItemLink
    >
    <DropdownItemLink
      v-for="[theme, ThemeIcon] of themes"
      :key="theme"
      :selected="theme === currentTheme"
      @click="setTheme(theme)"
      ><component :is="ThemeIcon" /> {{ t(`theme.${theme}`) }}</DropdownItemLink
    >
  </DropdownMenu>
</template>
