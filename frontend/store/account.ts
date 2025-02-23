import { computed, ref } from 'vue';

import { type AccountData, getAccountData } from '../api';

export const accountData = ref<AccountData | null>(null);

export const authenticated = computed(() => accountData.value !== null);

/**
 * Initialize the account data store
 */
export async function initialize(): Promise<void> {
  if (window.accountData !== undefined) {
    // Account data has been provided on the window object (by the backend patching index.html):
    // Move data to reactive reference exported above.
    accountData.value = window.accountData;
    delete window.accountData;
  } else {
    // Account data has not been provided, we need to request it
    await refresh();
  }
}

export async function refresh(): Promise<void> {
  accountData.value = await getAccountData();
}
