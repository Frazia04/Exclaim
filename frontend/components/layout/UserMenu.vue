<script setup lang="ts">
import AccountIcon from '@material-symbols/svg-400/outlined/account_circle.svg';
import AdminIcon from '@material-symbols/svg-400/outlined/admin_panel_settings.svg';
import LogoutIcon from '@material-symbols/svg-400/outlined/logout.svg';
import SettingsIcon from '@material-symbols/svg-400/outlined/settings.svg';

import { t } from '../../i18n';
import { settingsRouteName, userAdminRouteName } from '../../router/names';
import { accountData } from '../../store/account';
import DropdownItemLink from './DropdownItemLink.vue';
import DropdownItemText from './DropdownItemText.vue';
import DropdownMenu from './DropdownMenu.vue';

// TODO: Once everything has been migrated to the new frontend, integrate logout without reloading the whole page.
// But we still need to be able to redirect to SAML single logout.
function logout(): void {
  const form = document.createElement('form');
  form.action = 'logout';
  form.method = 'POST';

  const input = document.createElement('input');
  input.type = 'hidden';
  input.name = '_csrf';
  input.value = window.csrfToken ?? '';
  form.appendChild(input);

  document.body.appendChild(form);

  form.submit();
}
</script>

<template>
  <DropdownMenu v-if="accountData" id="user-menu">
    <template #button-content
      ><AccountIcon /><span v-text="` ${accountData.firstname} ${accountData.lastname}`"
    /></template>
    <DropdownItemText :text="`${accountData.firstname} ${accountData.lastname}`" />
    <!-- <RouterLink v-slot="{ navigate, route }" to="/settings" custom
      ><DropdownItemLink :href="route.href" @click="navigate"
        ><SettingsIcon /> {{ t('common.settings') }}</DropdownItemLink
      ></RouterLink
    > -->
    <RouterLink :to="{ name: settingsRouteName }"
      ><DropdownItemLink href="settings"><SettingsIcon /> {{ t('common.settings') }}</DropdownItemLink></RouterLink
    >
    <!-- <RouterLink v-if="accountData.isAdmin || accountData.isAssistant" v-slot="{ navigate, route }" to="/user" custom
      ><DropdownItemLink :href="route.href" @click="navigate"
        ><AdminIcon /> {{ t('common.user-administration') }}</DropdownItemLink
      ></RouterLink
    > -->
    <RouterLink v-if="accountData.isAdmin || accountData.isAssistant" :to="{ name: userAdminRouteName }">
      <DropdownItemLink href="useradmin"> <AdminIcon /> {{ t('common.user-administration') }} </DropdownItemLink>
    </RouterLink>

    <!-- <DropdownItemLink v-if="accountData.isAdmin || accountData.isAssistant" href="user"
      ><AdminIcon /> {{ t('common.user-administration') }}</DropdownItemLink
    > -->
    <DropdownItemLink @click="logout"><LogoutIcon /> {{ t('common.logout') }}</DropdownItemLink>
  </DropdownMenu>
</template>
