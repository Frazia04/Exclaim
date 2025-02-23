// This module is for managing the user's theme selection.
// - We add the selected theme name to the DOM `html` element's `data-theme` attribute such that
//   CSS selectors can depend on it. The value in DOM is considered the single source of truth.
// - We export a reactive reference `theme` holding the current theme name (read from DOM).
// - When updating the `theme` reference, we also persist the selected theme into the browser's
//   local storage such that we can restore it on a later visit. We use local storage to remember
//   the user's selection per device. A users might have multiple devices with different theme
//   preference, so a per-user database field would not be a good option.

import { watch } from 'vue';

import { createDomBackedRef } from '../utils/domBackedRef';

// The available theme names
export const themes = ['auto', 'light', 'dark'] as const;
export type Theme = (typeof themes)[number];

// The local storage key where we store the selected theme across visits
const localStorageKey = 'theme';

// The currently selected theme (stored in the `html` element's `data-theme` attribute)
export const theme = createDomBackedRef<Theme>(
  document.documentElement,
  'data-theme',

  // Determine the initial theme to be used (from local storage)
  (() => {
    // Look up last selected theme from local storage
    const restoredTheme = localStorage.getItem(localStorageKey);
    if (restoredTheme) {
      // Check that the theme is valid
      for (const t of themes) {
        if (t === restoredTheme) {
          return t;
        }
      }

      // Remove invalid theme form local storage
      localStorage.removeItem(localStorageKey);
    }

    // Use first theme if the local storage does not hold a valid theme name
    return themes[0];
  })(),
);

// Persist changes to local storage
watch(theme, (t) => localStorage.setItem(localStorageKey, t), { flush: 'sync' });

// Switch to the next theme
export function toggleTheme(): void {
  theme.value = themes[(themes.indexOf(theme.value) + 1) % themes.length];
}
