<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { login } from '../../../api';
import { language, t } from '../../../i18n';
import { getSuccessfulLoginRoute } from '../../../router/redirect-to-login';
import { accountData } from '../../../store/account';
import { configuration } from '../../../store/configuration';
import RPTULogo from '../../svg/RPTULogo.vue';

const router = useRouter();

// Form data
const username = ref('');
const password = ref('');

// Whether to show the login failed alert
const failed = ref(false);
function dismissFailed(): void {
  failed.value = false;
}
watch([username, password], dismissFailed);

// Check whether we have been redirected from logout
const isLogout = computed(() => 'logout' in router.currentRoute.value.query);
async function dismissLogout(): Promise<void> {
  await router.replace({ query: {} });
}

// Login with SAML account
const loginWithTranslation = computed(() => t('login.saml-auth').split('{0}', 2));

// Prevent multiple simultaneous submissions
let submitting = false;

async function submit(): Promise<void> {
  if (!submitting) {
    submitting = true;
    try {
      const result = await login({
        username: username.value,
        password: password.value,
      });
      if (result) {
        // Update language
        if (result.language) {
          language.value = result.language;
        }

        // Update account data
        accountData.value = result;

        // Navigate to remembered or home route
        await router.push(getSuccessfulLoginRoute());
      } else {
        failed.value = true;
      }
    } finally {
      submitting = false;
    }
  }
}

function samlAuth(registrationId: string): void {
  window.location.href = 'saml2/authenticate/' + registrationId;
}
</script>

<template>
  <main class="login">
    <h1>{{ t('login.login') }}</h1>
    <p v-if="isLogout" class="alert-success" @click="dismissLogout">{{ t('login.logged-out') }}</p>
    <div v-if="configuration.samlRegistrationIds.length" class="saml-auth">
      <template v-for="registrationId of configuration.samlRegistrationIds" :key="registrationId">
        <button v-if="registrationId === 'RHRK'" @click="samlAuth(registrationId)">
          {{ loginWithTranslation[0] }}<RPTULogo />{{ loginWithTranslation[1] }}
        </button>
        <button v-else @click="samlAuth(registrationId)" v-text="t('login.saml-auth', registrationId)"></button>
      </template>
    </div>
    <form @submit.prevent="submit">
      <div class="form-fields">
        <label for="username">{{ t('common.username') }}</label>
        <input id="username" v-model="username" />
        <label for="password">{{ t('common.password') }}</label>
        <input id="password" v-model="password" type="password" />
      </div>
      <input type="submit" :value="t('login.login')" />
    </form>
    <p v-if="failed" class="alert-danger" @click="dismissFailed">{{ t('login.failed') }}</p>
  </main>
</template>
