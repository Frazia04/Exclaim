// This module manages translations.

// We do not use a library because popular choices have, at the time of implementing this module, major drawbacks:
// - vue-i18n does not provide type-safety for translation keys:
//   https://github.com/intlify/vue-i18n-next/issues/1210#issuecomment-1315659205
// - i18next-vue does not provide type-safety for translation keys:
//   https://github.com/i18next/i18next-vue/issues/4
// - Building our own reactivity wrapper on top of i18next is cumbersome, the library is huge and type support
//   is suboptimal: https://github.com/i18next/i18next/issues/1883 https://github.com/i18next/i18next/issues/1901

// The actual translations are in the `translations` subdirectory.

import { computed, type Ref, ref, watch } from 'vue';

import type { Language } from '../api/types';
export type { Language } from '../api/types';
import { createDomBackedRef } from '../utils/domBackedRef';
import { type Lazy, type Translation, type Translations, translations } from './translations';

/**
 * The languages we support
 */
export const languages = ['en', 'de'] as const satisfies Readonly<Language[]>;

/**
 * The local storage key where we store the selected language across visits
 */
const localStorageKey = 'language';

/**
 * The currently selected language (stored in the `html` element's `lang` attribute)
 */
export const language = createDomBackedRef<Language>(
  document.documentElement,
  'lang',

  // Determine the initial language to be used
  (() => {
    // We try the following choices until one of them succeeds:
    // - language of the authenticated user from database (provided in `window.accountData.language` by the backend)
    // - last used language in local storage (for guest users)
    // - language preferences of the user's browser
    for (let candidate of [
      window.accountData?.language,
      localStorage.getItem(localStorageKey),
      ...navigator.languages,
    ]) {
      // Omit the region from the language and convert it to lower case
      let match: RegExpMatchArray | null;
      if (candidate && (match = candidate.match(/^(\w+)\b/))) {
        candidate = match[0].toLocaleLowerCase();

        // Check whether we support that language
        for (const supportedLanguage of languages) {
          if (candidate === supportedLanguage) {
            return candidate;
          }
        }
      }
    }

    // Use English as default
    return 'en';
  })(),
);

// Persist changes to local storage
watch(language, (newLanguage) => localStorage.setItem(localStorageKey, newLanguage), { flush: 'sync' });

// Construct type-safe translation keys
type DotPrefix<T extends string> = T extends '' ? '' : `.${T}`;
type ConstructKeys<T> = T extends Translation
  ? ''
  : T extends Lazy<infer U>
    ? ConstructKeys<U>
    : T extends Record<infer K, infer V>
      ? `${Extract<K, string>}${DotPrefix<ConstructKeys<V>>}`
      : never;
export type TranslationKey = ConstructKeys<typeof translations>;

// We extend the `Translations` type to mark lazy translations that are currently being resolved.
const resolvingSymbol = Symbol(__DEV__ ? 'resolving' : '');
type UntypedTranslation = Record<string, string>;
type ResolvingTranslations = {
  [key: string]:
    | UntypedTranslation
    | ResolvingTranslations
    | Lazy<Translations>
    | {
        [resolvingSymbol]: Ref<boolean>;
      };
};

// Resolve a translation key to a computed reference which computes the raw translation depending
// on the currently selected language. We cache the result for faster lookups. If we encounter a
// lazily loaded subtree, then we give an empty translation (the function is not async!) but make
// sure that the function gets reactively re-evaluated once the translations subtree is available.
const resolvedTranslationsCache = new Map<string, Ref<string>>();
function resolveKey(key: TranslationKey): Pick<Ref<string>, 'value'> {
  // Check the cache whether the key has already been resolved
  const resolved = resolvedTranslationsCache.get(key);
  if (resolved) {
    return resolved;
  }

  // Follow the path defined via the dots in the key
  let node: ResolvingTranslations | UntypedTranslation | undefined = translations;
  for (const keyPart of key.split('.')) {
    if (node) {
      const value: ResolvingTranslations[string] | string | undefined = node[keyPart];

      // Check whether there is a subtree to be resolved
      if (typeof value === 'function') {
        // Start resolving
        const promise = value();

        // Create a reactive reference to be triggered when the promise has resolved. We access it
        // such that the current function invocation gets re-evaluated after resolving the promise.
        const isReady = ref(false);
        node[keyPart] = { [resolvingSymbol]: isReady };
        void isReady.value;

        // After the promise has resolved, update the translations node and trigger the isReady ref
        void (async () => {
          try {
            node[keyPart] = await promise;
            isReady.value = true;
          } catch (error) {
            // Error handling without translations, otherwise we end up in a cycle
            if (__DEV__) {
              console.error(error);
            }
            alert(
              `An unexpected error has occurred while loading translations: ${error instanceof Error ? error.message : String(error)}`,
            );
          }
        })();

        // Use an empty string while resolving
        return { value: '' };
      }

      if (typeof value === 'object') {
        // Check whether that subtree is currently resolving
        if (resolvingSymbol in value) {
          // Access the reactive reference such that the current function invocation gets
          // re-evaluated after resolving.
          void value[resolvingSymbol].value;

          // Use an empty string while resolving
          return { value: '' };
        }

        // Follow the key part to the next node
        node = value;
      } else {
        // The key is invalid!
        node = undefined;
      }
    } else {
      break;
    }
  }

  // Create a computed reference that depends on the current language
  const result = computed(() => {
    // Access the language, such that computed gets re-evaluated on language change
    const l = language.value;

    // Hopefully, we now have a translation for that language
    const value = node?.[l];
    if (typeof value === 'string') {
      return value;
    }

    // Log invalid keys and use the key itself as translation
    if (__DEV__) {
      console.error(`Unknown translation key ${key} in language ${l}`);
    }
    return key;
  });

  // Cache the result
  resolvedTranslationsCache.set(key, result);
  return result;
}

/**
 * Retrieve the translation with the given key.
 * @param key key to retrieve
 * @param args ordinal parameters
 */
export function t(key: TranslationKey, ...args: string[]): string;

/**
 * Retrieve the translation with the given key.
 * @param key key to retrieve
 * @param args named parameters
 */
export function t(key: TranslationKey, args: Record<string, string>): string;

// implementation
export function t(key: TranslationKey, ...args: [Record<string, string>] | string[]): string {
  // Resolve the translation (this reactively depends on the currently selected language)
  let translation = resolveKey(key).value;

  // Insert arguments, if any
  if (args.length) {
    if (typeof args[0] === 'object') {
      // named parameters
      for (const [key, value] of Object.entries(args[0])) {
        translation = translation.replace(`{${key}}`, value);
      }
    } else {
      // ordinal parameters
      for (let i = 0; i < args.length; i++) {
        translation = translation.replace(`{${i}}`, (args as string[])[i]);
      }
    }
  }

  return translation;
}

/**
 * Retrieve the name of a language in its own translation.
 * @param lang The language code
 * @returns The language name in that language
 */
export function getLanguageName(lang: Language): string {
  return translations.meta['language-name'][lang];
}
