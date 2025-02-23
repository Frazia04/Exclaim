<script setup lang="ts">
import LanguageIcon from '@material-symbols/svg-400/outlined/language.svg';

import { setLanguage as apiSetLanguage } from '../../api';
import { getLanguageName, type Language, language as currentLanguage, languages, t } from '../../i18n';
import { authenticated } from '../../store/account';
import DropdownItemLink from './DropdownItemLink.vue';
import DropdownMenu from './DropdownMenu.vue';

function setLanguage(newLanguage: Language): void {
  // Change in frontend (also saves to localStorage in watcher)
  currentLanguage.value = newLanguage;

  // Tell backend if logged in, ignoring any (http) errors
  if (authenticated.value) {
    void apiSetLanguage(newLanguage);
  }
}
</script>

<template>
  <DropdownMenu id="language-switcher" :aria-label="`${t('common.select-language')} (${t('meta.language-name')})`">
    <template #button-content><LanguageIcon /></template>
    <DropdownItemLink
      v-for="language of languages"
      :key="language"
      :selected="language === currentLanguage"
      :text="getLanguageName(language)"
      @click="setLanguage(language)"
    />
  </DropdownMenu>
</template>
