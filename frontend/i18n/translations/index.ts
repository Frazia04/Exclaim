// Translations for our application.

// Translations imported via the lazy/* modules are loaded dynamically,
// the other ones will be included in the main entry chunk.

import type { Language } from '..';
import common from './common';
import errors from './errors';
import footer from './footer';
import type { Lazy } from './lazy';
export type { Lazy } from './lazy';
import courses from './courses';
import { exercise, home } from './lazy/student';
import login from './login';
import meta from './meta';
import theme from './theme';
import user from './user';

/**
 * A single translation holds a string for each language.
 */
export type Translation = Record<Language, string>;

/**
 * A translations object has string keys. Possible values are:
 * - A single `Translation`
 * - Another `Translations` object
 * - A `Lazy<Translations>`, i.e. a `Translations` object that is loaded dynamically
 */
export type Translations = {
  [key: string]: Translation | Translations | Lazy<Translations>;
};

/**
 *
 */
export const translations = {
  common,
  errors,
  footer,
  home,
  login,
  meta,
  theme,
  courses,
  exercise,
  user,
} satisfies Translations;

// Define the message structure based on your translation files
// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface MessageSchema {
  user: {
    admin: {
      title: string;
    };
    view: string;
    edit: string;
    delete: string;
    create: string;
  };
}
