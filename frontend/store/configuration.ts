import { ref } from 'vue';

import { type Configuration, getConfiguration } from '../api';

// Contract: Ref needs to be initialized before it is read. Initialization happens in main.ts.
export const configuration = ref<Readonly<Configuration>>(undefined as unknown as Configuration);

/**
 * Initialize the configuration store
 */
export async function initialize(): Promise<void> {
  if (window.frontendConfiguration !== undefined) {
    // Configuration has been provided on the window object (by the backend patching index.html):
    // Move data to reactive reference exported above.
    configuration.value = window.frontendConfiguration;
    delete window.frontendConfiguration;
  } else {
    // Configuration has not been provided, we need to request it
    await refresh();
  }
}

export async function refresh(): Promise<void> {
  configuration.value = await getConfiguration();
}
