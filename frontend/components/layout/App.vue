<script setup lang="ts">
import MailIcon from '@material-symbols/svg-400/outlined/mail.svg';
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';

import { t } from '../../i18n';
import { loading } from '../../router';
import { homeRouteName, loginRouteName } from '../../router/names';
import { authenticated } from '../../store/account';
import LoadingSpinner from '../general/LoadingSpinner.vue';
import ExclaimLogo from '../svg/ExclaimLogo.vue';
import LanguageSwitcher from './LanguageSwitcher.vue';
import ThemeSwitcher from './ThemeSwitcher.vue';
import UserMenu from './UserMenu.vue';

const route = useRoute();
const forceHomeRoute = computed(() => route.name === homeRouteName);
</script>

<template>
  <div id="app">
    <header>
      <div>
        <RouterLink
          :to="{ name: authenticated ? homeRouteName : loginRouteName, force: forceHomeRoute, replace: forceHomeRoute }"
          ><ExclaimLogo
        /></RouterLink>
        <ul>
          <UserMenu />
          <LanguageSwitcher />
          <ThemeSwitcher />
        </ul>
      </div>
    </header>

    <LoadingSpinner v-if="loading" />
    <RouterView v-else />

    <footer>
      <ExclaimLogo />
      <a href="mailto:exclaim@cs.uni-kl.de"><MailIcon /> {{ t('footer.contact') }}</a>
      <a :href="t('footer.imprint-url')" :text="t('footer.imprint')" />
      <a :href="t('footer.privacy-policy-url')" :text="t('footer.privacy-policy')" />
    </footer>
  </div>
</template>
