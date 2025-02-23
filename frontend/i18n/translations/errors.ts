import type { Translations } from '.';

export default {
  'session-expired': {
    en: 'Your session has expired. Safe your input (if any) and reload the page.',
    de: 'Ihre Sitzung ist abgelaufen. Sichern Sie Ihre Eingaben (falls relevant) und laden Sie die Seite neu.',
  },
  other: {
    en: 'An unexpected error has occurred. Please retry or safe your input (if any) and reload the page.',
    de: 'Es ist ein unerwarteter Fehler aufgetreten. Bitte versuchen Sie es erneuten oder sichern Sie Ihre Eingaben (falls relevant) und laden Sie die Seite neu.',
  },
  'http-status': {
    en: 'HTTP status: {0}',
    de: 'HTTP Status: {0}',
  },
  details: {
    en: 'Error message: {0}',
    de: 'Fehlermeldung: {0}',
  },
} satisfies Translations;
