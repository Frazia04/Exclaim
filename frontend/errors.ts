import { UnexpectedHttpStatusError } from './api/fetch';
import { t } from './i18n';

export function errorHandler(error: unknown): void {
  if (__DEV__) {
    console.error(error);
  }

  if (error instanceof UnexpectedHttpStatusError) {
    if (!error.reloading) {
      alert(
        error.status === 401
          ? t('errors.session-expired')
          : `${t('errors.other')} (${t('errors.http-status', `${error.status}`)})`,
      );
    }
  } else {
    alert(`${t('errors.other')} (${t('errors.details', error instanceof Error ? error.message : String(error))})`);
  }
}
